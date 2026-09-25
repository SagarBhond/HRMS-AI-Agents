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

- `AWS_ROLE_ARN`
- `AWS_ACCOUNT_ID`
- `AWS_REGION`
- `FRONTEND_BUCKET`

Do not put `GOOGLE_API_KEY`, `JWT_SECRET`, `DB_PASSWORD`, `DB_USERNAME`, or
`DB_NAME` in frontend secrets. A Vite frontend bundle is public to every
browser user.

## GitHub Actions

Create these repository or environment secrets:

- `AWS_ROLE_ARN` - the repository's GitHub Actions role ARN
- `AWS_ACCOUNT_ID`
- `AWS_REGION`
- `GOOGLE_API_KEY`
- `JWT_SECRET`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_NAME`
- `FRONTEND_BUCKET`
- `ORCHESTRATOR_PUBLIC_URL`

GitHub username/password credentials are not required. GitHub Actions should
authenticate to AWS with OIDC and `AWS_ROLE_ARN`; never store a GitHub password
in repository secrets.

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
