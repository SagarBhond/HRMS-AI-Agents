# Deployment configuration

Do not commit passwords, API keys, access keys, or tokens. Configure these values
in the local `.env` file or in GitHub repository/environment secrets.

## Local Docker Compose

Copy `.env.example` to `.env` and set:

- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_API_KEY`

Run `.\run-stack.ps1` on Windows or `./run-stack.sh` on Linux/macOS. The script
starts MySQL first, then MCP and authentication, then agents, the orchestrator,
and finally the frontend. It does not remove the existing MySQL volume.

## Where each secret belongs

Store backend, AWS, database, and deployment secrets in the **backend
repository** (`SagarBhond/HRMS-AI-Agents`) under **Settings -> Secrets and
variables -> Actions**. Prefer an environment named `production` and protect
it with required reviewers.

Store only frontend deployment secrets in the **frontend repository**:

- `FRONTEND_EC2_HOST` - the frontend EC2 public IP or DNS name
- `FRONTEND_EC2_SSH_KEY` - the private key matching Terraform's frontend public key
- `FRONTEND_EC2_KNOWN_HOSTS` - the verified SSH host-key line

Do not put `GOOGLE_API_KEY`, `JWT_SECRET`, `DB_PASSWORD`, `DB_USERNAME`, or
`DB_NAME` in frontend secrets. A Vite frontend bundle is public to every
browser user.

## GitHub Actions

The backend deployment workflow needs these repository or environment secrets:

- `AWS_ACCOUNT_ID` - ECR registry account
- `ORCHESTRATOR_PUBLIC_URL` - backend health-check URL

The backend build/test job does not receive `GOOGLE_API_KEY`; live Gemini
integration tests are excluded so a deployment does not consume model quota.
The key is configured in Terraform and passed to running backend services via
AWS Secrets Manager. Other issue-triage or release-analysis workflows may still
use the repository secret when their events run. GitHub Actions authenticates
to AWS with OIDC; never store a GitHub password in repository secrets.

## Local generated credentials

Run `.\setup-and-run.ps1` from the backend directory. It generates random
local values for `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`, and `JWT_SECRET`,
asks only for `GOOGLE_API_KEY`, writes them to the ignored `.env`, and starts
the stack. There is no safe universal password to publish in this document.

Set `$env:REPAIR_EXISTING_DB = "1"` before running the script only when the
existing MySQL volume needs the `%` host grant repaired.

## Terraform

The Terraform variables `db_password`, `google_api_key`, and `jwt_secret` are
sensitive and must be supplied through `TF_VAR_...` environment variables or a
secret-backed CI step. `db_username` is normally `hrms` and `db_name` is
normally `hrms_db`; these are Terraform variables, not hard-coded credentials.
Set `github_org`, `github_repo`, and `github_branch` so the OIDC trust policy
only accepts the intended repository and branch. The current manually managed
role and provider values are:

- Backend role:
  `arn:aws:iam::882040517001:role/hrms-backend-github-actions`
- Frontend role:
  `arn:aws:iam::882040517001:role/hrms-frontend-github-actions`
- GitHub OIDC provider:
  `arn:aws:iam::882040517001:oidc-provider/token.actions.githubusercontent.com`

The provider ARN belongs only in the role trust relationship. It must not be
placed in an identity or inline permissions policy. Manual trust-policy
templates are in `terraform/github-oidc-trust-policy.example.json` and
`terraform/github-oidc-frontend-trust-policy.example.json`.

The frontend is hosted by an Amazon Linux EC2 instance (`t3.micro` by
default) behind the existing load balancer. Configure these repository
secrets for frontend deployment:

- `FRONTEND_EC2_HOST`: the instance public IP from Terraform output
  `frontend_instance_public_ip`.
- `FRONTEND_EC2_SSH_KEY`: the matching private SSH key.
- `FRONTEND_EC2_KNOWN_HOSTS`: the verified SSH host-key line for that instance.

The `frontend_ssh_cidr` Terraform variable defaults to `0.0.0.0/0` because
GitHub-hosted runner addresses change. Use a fixed runner and a narrower CIDR
where possible. Frontend assets are copied directly over SSH; no S3 bucket is
used.

Terraform looks up the existing EC2 key pair named `pro` in the configured AWS
region; it does not create a key pair. The private PEM file matching that AWS
key pair belongs only in the `FRONTEND_EC2_SSH_KEY` GitHub secret and must not
be committed or added to Terraform variables.

If Terraform reports that `hrms/application` is scheduled for deletion, restore
and import the existing secret before applying. With the AWS CLI configured
for the Terraform region, run:

```powershell
aws secretsmanager restore-secret --secret-id hrms/application --region ap-south-1
terraform import aws_secretsmanager_secret.application hrms/application
terraform apply
```

The secret resource uses `prevent_destroy` to avoid scheduling it for deletion
again. The import records the restored secret in Terraform state; apply then
updates its version using the currently supplied `TF_VAR_...` values.
