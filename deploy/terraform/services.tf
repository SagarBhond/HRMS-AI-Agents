resource "aws_ecs_task_definition" "service" {
  for_each                 = merge(local.services, { frontend = { port = 80, cpu = 256, memory = 512, public = true } })
  family                   = "${var.project_name}-${each.key}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  execution_role_arn       = aws_iam_role.execution.arn
  container_definitions = jsonencode([{
    name         = each.key
    image        = "${aws_ecr_repository.service[each.key].repository_url}:${each.key == "frontend" ? var.frontend_image_tag : var.backend_image_tag}"
    essential    = true
    portMappings = [{ containerPort = each.value.port, hostPort = each.value.port, protocol = "tcp" }]
    environment = each.key == "frontend" ? [] : [
      { name = "GOOGLE_API_KEY", value = var.google_api_key },
      { name = "JWT_SECRET", value = var.jwt_secret },
      { name = "HRMS_DATASOURCE_URL", value = "jdbc:mysql://${aws_db_instance.mysql.address}:3306/hrms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" },
      { name = "HRMS_DATASOURCE_USERNAME", value = var.db_username },
      { name = "HRMS_DATASOURCE_PASSWORD", value = var.db_password },
      { name = "HRMS_CORS_ALLOWED_ORIGINS", value = "http://${aws_lb.main.dns_name}" },
      { name = "AUTH_CORS_ALLOWED_ORIGIN", value = "http://${aws_lb.main.dns_name}" },
      { name = "MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "EMPLOYEE_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "ATTENDANCE_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "PAYROLL_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "MANAGER_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "LEAVE_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "HR_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "DOCUMENT_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "NOTIFICATION_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "POLICY_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "EXPENSE_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "ASSET_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "PERFORMANCE_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "RECRUITMENT_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "AUDIT_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "COMPLIANCE_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "WORKFLOW_MCP_SERVER_URL", value = "http://mcp-server.${aws_service_discovery_private_dns_namespace.main.name}:8082" },
      { name = "EMPLOYEE_AGENT_A2A_URL", value = "http://employee-agent.${aws_service_discovery_private_dns_namespace.main.name}:8083" },
      { name = "ATTENDANCE_AGENT_A2A_URL", value = "http://attendance-agent.${aws_service_discovery_private_dns_namespace.main.name}:8084" },
      { name = "PAYROLL_AGENT_A2A_URL", value = "http://payroll-agent.${aws_service_discovery_private_dns_namespace.main.name}:8085" },
      { name = "HR_AGENT_A2A_URL", value = "http://hr-agent.${aws_service_discovery_private_dns_namespace.main.name}:8088" },
      { name = "LEAVE_AGENT_A2A_URL", value = "http://leave-agent.${aws_service_discovery_private_dns_namespace.main.name}:8087" },
      { name = "MANAGER_AGENT_A2A_URL", value = "http://manager-agent.${aws_service_discovery_private_dns_namespace.main.name}:8086" },
      { name = "DOCUMENT_AGENT_A2A_URL", value = "http://document-agent.${aws_service_discovery_private_dns_namespace.main.name}:8089" },
      { name = "NOTIFICATION_AGENT_A2A_URL", value = "http://notification-agent.${aws_service_discovery_private_dns_namespace.main.name}:8090" },
      { name = "POLICY_AGENT_A2A_URL", value = "http://policy-agent.${aws_service_discovery_private_dns_namespace.main.name}:8091" },
      { name = "EXPENSE_AGENT_A2A_URL", value = "http://expense-agent.${aws_service_discovery_private_dns_namespace.main.name}:8092" },
      { name = "ASSET_AGENT_A2A_URL", value = "http://asset-agent.${aws_service_discovery_private_dns_namespace.main.name}:8093" },
      { name = "PERFORMANCE_AGENT_A2A_URL", value = "http://performance-agent.${aws_service_discovery_private_dns_namespace.main.name}:8094" },
      { name = "RECRUITMENT_AGENT_A2A_URL", value = "http://recruitment-agent.${aws_service_discovery_private_dns_namespace.main.name}:8095" },
      { name = "AUDIT_AGENT_A2A_URL", value = "http://audit-agent.${aws_service_discovery_private_dns_namespace.main.name}:8096" },
      { name = "COMPLIANCE_AGENT_A2A_URL", value = "http://compliance-agent.${aws_service_discovery_private_dns_namespace.main.name}:8097" },
      { name = "WORKFLOW_AGENT_A2A_URL", value = "http://workflow-agent.${aws_service_discovery_private_dns_namespace.main.name}:8098" }
    ]
    logConfiguration = { logDriver = "awslogs", options = { awslogs-group = aws_cloudwatch_log_group.service[each.key].name, awslogs-region = var.aws_region, awslogs-stream-prefix = "ecs" } }
  }])
}

resource "aws_service_discovery_service" "service" {
  for_each = local.services
  name     = each.key
  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id
    dns_records {
      ttl  = 10
      type = "A"
    }
    routing_policy = "MULTIVALUE"
  }
}

resource "aws_ecs_service" "service" {
  for_each        = merge(local.services, { frontend = { port = 80, cpu = 256, memory = 512, public = true } })
  name            = "${var.project_name}-${each.key}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.service[each.key].arn
  desired_count   = 1
  launch_type     = "FARGATE"
  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = true
  }
  dynamic "service_registries" {
    for_each = contains(keys(local.services), each.key) ? [1] : []
    content { registry_arn = aws_service_discovery_service.service[each.key].arn }
  }
  load_balancer {
    target_group_arn = aws_lb_target_group.service[each.key].arn
    container_name   = each.key
    container_port   = each.value.port
  }
  depends_on = [aws_lb_listener.http]
}

resource "aws_lb_listener_rule" "auth" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 10
  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.service["auth-service"].arn
  }
  condition {
    path_pattern { values = ["/api/auth/*"] }
  }
}

resource "aws_lb_listener_rule" "orchestrator" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 20
  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.service["orchestrator-agent"].arn
  }
  condition {
    path_pattern { values = ["/api/v1/orchestrator/*"] }
  }
}

output "alb_url" { value = "http://${aws_lb.main.dns_name}" }
output "frontend_url" { value = "http://${aws_lb.main.dns_name}" }
output "ecr_repositories" { value = { for name, repository in aws_ecr_repository.service : name => repository.repository_url } }
output "rds_endpoint" { value = aws_db_instance.mysql.address }
