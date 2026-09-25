resource "aws_secretsmanager_secret" "application" {
  name                    = "${var.project_name}/application"
  description             = "HRMS database and application credentials"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "application" {
  secret_id = aws_secretsmanager_secret.application.id
  secret_string = jsonencode({
    db_username    = var.db_username
    db_password    = var.db_password
    google_api_key = var.google_api_key
    jwt_secret     = var.jwt_secret
  })
}

resource "aws_cloudwatch_log_group" "service" {
  for_each          = local.services
  name              = "/ecs/${var.project_name}/${each.key}"
  retention_in_days = 14
}
