resource "aws_ecr_repository" "service" {
  for_each = local.services
  name     = "${var.project_name}/${each.key}"
}
