data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-kernel-*-x86_64"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

data "aws_key_pair" "frontend" {
  key_name = "pro"
}

resource "aws_instance" "frontend" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = var.frontend_instance_type
  subnet_id                   = aws_subnet.public[0].id
  vpc_security_group_ids      = [aws_security_group.frontend.id]
  key_name                    = data.aws_key_pair.frontend.key_name
  associate_public_ip_address = true
  user_data                   = file("${path.module}/scripts/frontend_user_data.sh")
  user_data_replace_on_change = true

  tags = {
    Name = "${var.project_name}-frontend"
  }
}