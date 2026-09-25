output "load_balancer_url" {
  value = "http://${aws_lb.this.dns_name}"
}

output "github_actions_role_arn" {
  value = var.github_actions_role_arn
}

output "github_oidc_trust_target" {
  value = "repo:${var.github_org}/${var.github_repo}:ref:refs/heads/${var.github_branch}"
}

output "application_secret_arn" {
  value = aws_secretsmanager_secret.application.arn
}

output "ecr_repository_urls" {
  value = { for name, repo in aws_ecr_repository.service : name => repo.repository_url }
}

output "local_stack_urls" {
  value = {
    frontend               = "http://localhost:3000"
    auth_backend           = "http://localhost:8095"
    orchestrator_backend   = "http://localhost:8096"
    employee_agent         = "http://localhost:8097"
    manager_agent_backend  = "http://localhost:8098"
    prometheus             = "http://localhost:9090"
    grafana                = "http://localhost:3001"
    node_exporter_metrics  = "http://localhost:9100/metrics"
    swagger_auth           = "http://localhost:8095/swagger-ui/index.html"
    swagger_orchestrator   = "http://localhost:8096/swagger-ui/index.html"
    swagger_employee_agent = "http://localhost:8097/swagger-ui/index.html"
    swagger_manager_agent  = "http://localhost:8098/swagger-ui/index.html"
    prometheus_targets     = "http://localhost:9090/targets"
    grafana_ui             = "http://localhost:3001"
    load_balancer          = "http://${aws_lb.this.dns_name}"
  }
}

output "local_stack_url_summary" {
  value = <<EOT
For your HRMS-AI Docker Desktop local stack, the local URLs should be organized like this:

Component | Port | Local URL
--------- | ---- | ---------
Frontend | 3000 | http://localhost:3000
Auth / Backend | 8095 | http://localhost:8095
Orchestrator / Backend | 8096 | http://localhost:8096
Employee Agent | 8097 | http://localhost:8097
Manager/Agent Backend | 8098 | http://localhost:8098
Prometheus | 9090 | http://localhost:9090
Grafana | 3001 | http://localhost:3001
Node Exporter | 9100 | http://localhost:9100/metrics

Swagger URLs:
- http://localhost:8095/swagger-ui/index.html
- http://localhost:8096/swagger-ui/index.html
- http://localhost:8097/swagger-ui/index.html
- http://localhost:8098/swagger-ui/index.html

Load balancer URL:
- http://${aws_lb.this.dns_name}

The localhost values above are the local Docker Desktop endpoints. Replace the localhost host with the ALB hostname when you want to reach the same services through the AWS load balancer.
EOT
}
