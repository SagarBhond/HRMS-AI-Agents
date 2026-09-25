data "aws_availability_zones" "available" {
  state = "available"
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
