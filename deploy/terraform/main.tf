data "aws_availability_zones" "available" { state = "available" }
data "aws_caller_identity" "current" {}

data "aws_ecr_authorization_token" "current" {}

locals {
  services = {
    "auth-service"       = { port = 8081, cpu = 512, memory = 1024, public = true }
    "mcp-server"         = { port = 8082, cpu = 512, memory = 1024, public = false }
    "orchestrator-agent" = { port = 8080, cpu = 1024, memory = 2048, public = true }
    "employee-agent"     = { port = 8083, cpu = 512, memory = 1024, public = false }
    "attendance-agent"   = { port = 8084, cpu = 512, memory = 1024, public = false }
    "payroll-agent"      = { port = 8085, cpu = 512, memory = 1024, public = false }
    "manager-agent"      = { port = 8086, cpu = 512, memory = 1024, public = false }
    "leave-agent"        = { port = 8087, cpu = 512, memory = 1024, public = false }
    "hr-agent"           = { port = 8088, cpu = 512, memory = 1024, public = false }
    "document-agent"     = { port = 8089, cpu = 512, memory = 1024, public = false }
    "notification-agent" = { port = 8090, cpu = 512, memory = 1024, public = false }
    "policy-agent"       = { port = 8091, cpu = 512, memory = 1024, public = false }
    "expense-agent"      = { port = 8092, cpu = 512, memory = 1024, public = false }
    "asset-agent"        = { port = 8093, cpu = 512, memory = 1024, public = false }
    "performance-agent"  = { port = 8094, cpu = 512, memory = 1024, public = false }
    "recruitment-agent"  = { port = 8095, cpu = 512, memory = 1024, public = false }
    "audit-agent"        = { port = 8096, cpu = 512, memory = 1024, public = false }
    "compliance-agent"   = { port = 8097, cpu = 512, memory = 1024, public = false }
    "workflow-agent"     = { port = 8098, cpu = 512, memory = 1024, public = false }
  }
}

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
}

resource "aws_internet_gateway" "main" { vpc_id = aws_vpc.main.id }

resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index)
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
}

resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "alb" {
  name   = "${var.project_name}-alb"
  vpc_id = aws_vpc.main.id
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ecs" {
  name   = "${var.project_name}-ecs"
  vpc_id = aws_vpc.main.id
  ingress {
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "db" {
  name   = "${var.project_name}-db"
  vpc_id = aws_vpc.main.id
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_subnet_group" "main" {
  name       = var.project_name
  subnet_ids = aws_subnet.public[*].id
}

resource "aws_db_instance" "mysql" {
  identifier             = var.project_name
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t4g.micro"
  allocated_storage      = 20
  storage_type           = "gp3"
  db_name                = "hrms_db"
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.db.id]
  publicly_accessible    = false
  skip_final_snapshot    = true
  deletion_protection    = false
}

resource "aws_ecs_cluster" "main" { name = var.project_name }
resource "aws_service_discovery_private_dns_namespace" "main" {
  name = "${var.project_name}.local"
  vpc  = aws_vpc.main.id
}

resource "aws_ecr_repository" "service" {
  for_each = merge(local.services, { frontend = { port = 80 } })
  name     = "${var.project_name}/${each.key}"
  image_scanning_configuration { scan_on_push = true }
  force_delete = true
}

resource "aws_cloudwatch_log_group" "service" {
  for_each          = merge(local.services, { frontend = { port = 80 } })
  name              = "/ecs/${var.project_name}/${each.key}"
  retention_in_days = 14
}

resource "aws_iam_role" "execution" {
  name               = "${var.project_name}-ecs-execution"
  assume_role_policy = jsonencode({ Version = "2012-10-17", Statement = [{ Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" }, Action = "sts:AssumeRole" }] })
}

resource "aws_iam_role_policy_attachment" "execution" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_lb" "main" {
  name               = "${var.project_name}-alb"
  load_balancer_type = "application"
  subnets            = aws_subnet.public[*].id
  security_groups    = [aws_security_group.alb.id]
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.service["frontend"].arn
  }
}

resource "aws_lb_target_group" "service" {
  for_each    = merge(local.services, { frontend = { port = 80 } })
  name        = substr("${var.project_name}-${replace(each.key, "_", "-")}", 0, 32)
  port        = each.value.port
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.main.id
  health_check {
    path    = each.key == "frontend" ? "/healthz" : "/actuator/health"
    matcher = "200-399"
  }
}
