data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

data "aws_s3_bucket" "frontend" {
  bucket = var.frontend_bucket
}

locals {
  services = {
    auth         = { port = 8081, health = "/actuator/health" }
    mcp          = { port = 8082, health = "/actuator/health" }
    backend      = { port = 8080, health = "/actuator/health" }
    employee     = { port = 8083, health = "/actuator/health" }
    attendance   = { port = 8084, health = "/actuator/health" }
    payroll      = { port = 8085, health = "/actuator/health" }
    manager      = { port = 8086, health = "/actuator/health" }
    leave        = { port = 8087, health = "/actuator/health" }
    hr           = { port = 8088, health = "/actuator/health" }
    document     = { port = 8089, health = "/actuator/health" }
    notification = { port = 8090, health = "/actuator/health" }
    policy       = { port = 8091, health = "/actuator/health" }
    expense      = { port = 8092, health = "/actuator/health" }
    asset        = { port = 8093, health = "/actuator/health" }
    performance  = { port = 8094, health = "/actuator/health" }
    recruitment  = { port = 8095, health = "/actuator/health" }
    audit        = { port = 8096, health = "/actuator/health" }
    compliance   = { port = 8097, health = "/actuator/health" }
    workflow     = { port = 8098, health = "/actuator/health" }
    frontend     = { port = 80, health = "/health" }
  }
}

resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags                 = { Name = "${var.project_name}-vpc" }
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
}

resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.this.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index)
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }
}

resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "alb" {
  name   = "${var.project_name}-alb"
  vpc_id = aws_vpc.this.id
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
  vpc_id = aws_vpc.this.id
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

resource "aws_security_group" "rds" {
  name   = "${var.project_name}-rds"
  vpc_id = aws_vpc.this.id
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }
}

resource "aws_db_subnet_group" "this" {
  name       = "${var.project_name}-db"
  subnet_ids = aws_subnet.public[*].id
}

resource "aws_db_instance" "this" {
  identifier             = "${var.project_name}-mysql"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro"
  allocated_storage      = 20
  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  storage_encrypted      = true
  skip_final_snapshot    = true
}

resource "aws_ecs_cluster" "this" {
  name = var.project_name
}

resource "aws_ecr_repository" "service" {
  for_each = local.services
  name     = "${var.project_name}/${each.key}"
}

resource "aws_cloudwatch_log_group" "service" {
  for_each          = local.services
  name              = "/ecs/${var.project_name}/${each.key}"
  retention_in_days = 14
}

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

resource "aws_iam_role" "execution" {
  name = "${var.project_name}-ecs-execution"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

resource "aws_iam_role_policy_attachment" "execution" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "execution_secrets" {
  name = "${var.project_name}-ecs-secrets"
  role = aws_iam_role.execution.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue"]
      Resource = aws_secretsmanager_secret.application.arn
    }]
  })
}

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
      { name = "SPRING_DATASOURCE_URL", value = "jdbc:mysql://${aws_db_instance.this.address}:3306/${var.db_name}?useSSL=true" },
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

resource "aws_lb" "this" {
  name               = "${var.project_name}-alb"
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.this.arn
  port              = 80
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.service["frontend"].arn
  }
}

resource "aws_lb_target_group" "service" {
  for_each    = local.services
  name        = "${var.project_name}-${each.key}"
  port        = each.value.port
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.this.id
  health_check {
    path    = each.value.health
    matcher = "200"
  }
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

resource "aws_cloudfront_origin_access_control" "frontend" {
  count                             = var.enable_cloudfront ? 1 : 0
  name                              = "${var.project_name}-frontend-oac"
  description                       = "CloudFront access to the existing frontend S3 bucket"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "frontend" {
  count               = var.enable_cloudfront ? 1 : 0
  enabled             = true
  comment             = "${var.project_name} frontend"
  default_root_object = "index.html"

  origin {
    domain_name              = data.aws_s3_bucket.frontend.bucket_regional_domain_name
    origin_id                = "s3-${var.frontend_bucket}"
    origin_path              = "/${trim(var.frontend_prefix, "/")}"
    origin_access_control_id = aws_cloudfront_origin_access_control.frontend[0].id
  }

  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD", "OPTIONS"]
    target_origin_id       = "s3-${var.frontend_bucket}"
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      query_string = true
      cookies {
        forward = "none"
      }
    }
  }

  custom_error_response {
    error_code         = 403
    response_code      = 200
    response_page_path = "/index.html"
  }

  custom_error_response {
    error_code         = 404
    response_code      = 200
    response_page_path = "/index.html"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }
}

resource "aws_s3_bucket_policy" "frontend_cloudfront" {
  count  = var.enable_cloudfront ? 1 : 0
  bucket = data.aws_s3_bucket.frontend.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid    = "AllowCloudFrontServicePrincipalReadOnly"
      Effect = "Allow"
      Principal = {
        Service = "cloudfront.amazonaws.com"
      }
      Action   = "s3:GetObject"
      Resource = "${data.aws_s3_bucket.frontend.arn}/${trim(var.frontend_prefix, "/")}/*"
      Condition = {
        StringEquals = {
          "AWS:SourceArn" = aws_cloudfront_distribution.frontend[0].arn
        }
      }
    }]
  })
}
