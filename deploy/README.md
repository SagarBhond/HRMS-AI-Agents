# HRMS deployment

The deployment source of truth is this directory. The old root Terraform files came from an order-saga project and are not used.

Repositories:

- Backend: https://github.com/SagarBhond/HRMS-AI-Agents.git
- Frontend: https://github.com/SagarBhond/borkarpranit-ShrijaAI_Model_Frontend.git

The backend and frontend are separate GitHub repositories. Terraform is run from the backend repository's `deploy/terraform` directory; it creates the shared AWS infrastructure and ECS service definitions for images from both repositories.

## Local Docker

From `HRMS-AI-Agents-main`:

```powershell
Copy-Item deploy/.env.example deploy/.env -ErrorAction SilentlyContinue
$envFile = "$HOME\hrms-secrets\.env"
docker compose --env-file $envFile -f deploy/docker-compose.database.yml up -d
docker compose --env-file $envFile -f deploy/docker-compose.database.yml ps
```

The database-only command starts exactly one container: `hrms-mysql-1`, using port `3306` and the persistent `mysql-data` volume. It does not start the backend, frontend, or monitoring containers.

When you are ready to run the complete local application, keep that one MySQL container and start the rest:

```powershell
docker compose --env-file $envFile -f deploy/docker-compose.yml up --build
```

The SPA is at `http://localhost:3000`. Auth is on `8081`, the orchestrator is on `8080`, and Grafana is started separately at `http://localhost:3001`:

```powershell
docker compose --env-file $envFile -f deploy/docker-compose.monitoring.yml up -d
```

Do not run a second MySQL Compose project. Both Compose files use the same `hrms` project name and `mysql-data` volume.

## AWS Terraform

The Terraform stack provisions ECR, ECS/Fargate, an ALB, Cloud Map, and MySQL RDS. It expects images to exist in ECR before ECS tasks become healthy.

```powershell
cd deploy/terraform
terraform init
terraform fmt -recursive
terraform plan -var='db_password=...' -var='google_api_key=...' -var='jwt_secret=...'
terraform apply -var='db_password=...' -var='google_api_key=...' -var='jwt_secret=...'
```

Terraform creates a VPC, public subnets, routing, security groups, an internet-facing ALB, ECS cluster, Fargate task definitions/services, ECR repositories, Cloud Map service discovery, CloudWatch log groups, and a MySQL RDS instance. It does not build Docker images or create the GitHub OIDC role.

Build and push the backend images from the backend repository root with `backend/Dockerfile`, passing `SERVICE` and `SERVICE_PORT`. Build the frontend from the frontend repository root with `frontend/Dockerfile`; set `VITE_API_BASE_URL` to the ALB URL. The two workflows use this manually created OIDC role:

`arn:aws:iam::882040517001:role/github-actions-order-saga-role`

The role trust policy must allow GitHub's OIDC provider and the two repository subjects. The role also needs ECR push, ECS update, and read permissions. Do not commit secrets, Terraform state, or generated plans.

Recommended order:

1. Run Terraform and note the `alb_url` and ECR repository outputs.
2. Push backend code to `SagarBhond/HRMS-AI-Agents`; its workflow builds all backend images and refreshes ECS services.
3. Set the frontend repository secret `ORCHESTRATOR_PUBLIC_URL` to the ALB URL, then push `SagarBhond/borkarpranit-ShrijaAI_Model_Frontend`; its workflow builds and deploys the frontend image.
4. Check the ALB URL, `/actuator/health` on the orchestrator route, and ECS service events/logs.
