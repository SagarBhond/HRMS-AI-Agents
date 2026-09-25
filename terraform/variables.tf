variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "project_name" {
  type    = string
  default = "hrms"
}

variable "vpc_cidr" {
  type    = string
  default = "10.42.0.0/16"
}

variable "db_name" {
  type    = string
  default = "hrms_db"
}

variable "db_username" {
  type      = string
  sensitive = true
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "google_api_key" {
  type      = string
  sensitive = true
}

variable "jwt_secret" {
  type      = string
  sensitive = true
}

variable "github_org" {
  type = string
}

variable "github_repo" {
  type = string
}

variable "github_branch" {
  type    = string
  default = "main"
}

variable "github_actions_role_arn" {
  type    = string
  default = "arn:aws:iam::882040517001:role/hrms-backend-github-actions"
}

variable "frontend_bucket" {
  type    = string
  default = "sagar100001"
}

variable "frontend_prefix" {
  type    = string
  default = "sagar"
}

variable "enable_cloudfront" {
  type    = bool
  default = false
}

variable "service_images" {
  type    = map(string)
  default = {}
}

variable "desired_count" {
  type    = number
  default = 1
}
