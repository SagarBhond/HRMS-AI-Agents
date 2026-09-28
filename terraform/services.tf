resource "aws_ecs_task_definition" "service" {
  for_each                 = local.ecs_services
  family                   = "${var.project_name}-${each.key}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = each.key == "frontend" ? 256 : 512
  memory                   = each.key == "frontend" ? 512 : 1024
  execution_role_arn       = aws_iam_role.execution.arn

  container_definitions = jsonencode([{
    name         = each.key
    image        = lookup(var.service_images, each.key, "${aws_ecr_repository.service[each.key].repository_url}:latest")
    essential    = true
    portMappings = [{ containerPort = each.value.port, hostPort = each.value.port, protocol = "tcp" }]
    environment = concat(
      [
        { name = "SERVER_PORT", value = tostring(each.value.port) },
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:mysql://${aws_db_instance.this.address}:3306/${var.db_name}?useSSL=true" },
        { name = "MCP_SERVER_URL", value = local.mcp_server_url }
      ],
      [for name, url in local.agent_a2a_urls : { name = name, value = url }],
      [for prefix in values(local.service_env_prefixes) : { name = "${prefix}_MCP_SERVER_URL", value = local.mcp_server_url }]
    )
    secrets = [
      { name = "SPRING_DATASOURCE_USERNAME", valueFrom = "${aws_secretsmanager_secret.application.arn}:db_username::" },
      { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "${aws_secretsmanager_secret.application.arn}:db_password::" },
      { name = "GOOGLE_API_KEY", valueFrom = "${aws_secretsmanager_secret.application.arn}:google_api_key::" },
      { name = "JWT_SECRET", valueFrom = "${aws_secretsmanager_secret.application.arn}:jwt_secret::" }
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-group         = aws_cloudwatch_log_group.service[each.key].name
        awslogs-region        = var.aws_region
        awslogs-stream-prefix = each.key
      }
    }
  }])
}

resource "aws_ecs_service" "service" {
  for_each        = local.ecs_services
  name            = "${var.project_name}-${each.key}"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.service[each.key].arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = true
  }

  dynamic "load_balancer" {
    for_each = contains(keys(local.public_ecs_services), each.key) ? [each.value] : []

    content {
      target_group_arn = aws_lb_target_group.service[each.key].arn
      container_name   = each.key
      container_port   = load_balancer.value.port
    }
  }

  service_registries {
    registry_arn = aws_service_discovery_service.service[each.key].arn
  }

  depends_on = [
    aws_lb_listener_rule.auth_api,
    aws_lb_listener_rule.orchestrator_api
  ]
}

resource "aws_service_discovery_service" "service" {
  for_each = local.ecs_services
  name     = each.key

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.this.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}
