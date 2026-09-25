resource "aws_ecs_task_definition" "service" {
  for_each                 = local.services
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
    environment = each.key == "frontend" ? [] : [
      { name = "SERVER_PORT", value = tostring(each.value.port) },
      { name = "SPRING_DATASOURCE_URL", value = "jdbc:mysql://${aws_db_instance.this.address}:3306/${var.db_name}?useSSL=true" }
    ]
    secrets = each.key == "frontend" ? [] : [
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
  for_each        = local.services
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

  depends_on = [aws_lb_listener.http]
}
