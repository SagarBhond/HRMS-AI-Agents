# Deployment configuration

Do not commit passwords, API keys, access keys, or tokens. Configure these values
in the local `.env` file or in GitHub repository/environment secrets.

## Local Docker Compose

Copy `.env.example` to `.env` and set:
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
- Frontend role:
  `arn:aws:iam::882040517001:role/hrms-frontend-github-actions`

Do not put `GOOGLE_API_KEY`, `JWT_SECRET`, `DB_PASSWORD`, `DB_USERNAME`, or
`DB_NAME` in frontend secrets. A Vite frontend bundle is public to every
browser user.

## GitHub Actions

The backend deployment workflow needs these repository or environment secrets:

- `AWS_ACCOUNT_ID` - ECR registry account
- `ORCHESTRATOR_PUBLIC_URL` - backend health-check URL

The backend build/test job does not receive `GOOGLE_API_KEY`, so environment-
gated live Gemini tests are skipped during deployment. The key is configured
in Terraform and passed to running backend services via AWS Secrets Manager.
GitHub Actions authenticates to AWS with OIDC; never store a GitHub password
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
placed in an identity or inline permissions policy. The backend trust-policy
template is in `terraform/github-oidc-trust-policy.example.json`. These
templates use GitHub's immutable owner and repository ID subject format. Apply
the backend trust policy from the repository root with:

```powershell
aws iam update-assume-role-policy --role-name hrms-backend-github-actions --policy-document file://terraform/github-oidc-trust-policy.example.json
```

The frontend trust-policy template is in
`terraform/github-oidc-frontend-trust-policy.example.json`; it matches the
frontend role's currently working immutable repository subject.

Terraform creates the empty `hrms/frontend-deploy` secret container in
`ap-south-1`. After `terraform apply`, add a JSON value to it in the AWS
Secrets Manager console, or use the local helper below. The value must have
these fields:

```json
{
  "host": "current-frontend-ec2-public-dns",
  "username": "ec2-user",
  "private_key": "-----BEGIN RSA PRIVATE KEY-----\n...\n-----END RSA PRIVATE KEY-----",
  "known_hosts": "verified-known-hosts-line"
}
```

Replace the example values. The private key must be authorized on the EC2
instance, and the known-hosts value must contain verified SSH host-key line(s).
Encode newline characters in both multiline fields as `\n` in the JSON value;
Secrets Manager returns them as actual newlines when the workflow parses JSON.
Do not put these values in Terraform state, GitHub repository secrets, or Git.
The frontend workflow uses OIDC to assume the frontend role and fetch this
secret at deploy time. The Terraform permission grants access only to this
secret. The frontend OIDC trust policy is included for reference; the existing
frontend role already uses the matching immutable subject. If you need to
update it manually, run this from the repository root:

```powershell
aws iam update-assume-role-policy --role-name hrms-frontend-github-actions --policy-document file://terraform/github-oidc-frontend-trust-policy.example.json
```

To upload the secret from Windows without putting the private key in shell
history or Terraform state, save the rotated private key and verified
`ssh-keyscan` output in files outside the repository, then run from the backend
repository root:

```powershell
./terraform/scripts/set-frontend-deploy-secret.ps1 `
  -InstanceHost (terraform -chdir=terraform output -raw frontend_instance_public_ip) `
  -PrivateKeyPath "$HOME\.ssh\hrms-frontend-deploy" `
  -KnownHostsPath "$HOME\.ssh\hrms-frontend-known-hosts"
```

The helper writes a temporary JSON payload with user-only file permissions,
uploads it with the AWS CLI, and removes the temporary file. The replacement
public key must already be authorized for `ec2-user` on the instance. Never use
the private key previously pasted into chat; rotate the EC2 key and refresh the
host-key file for the current instance first.

Run `terraform apply` to create the secret container and attach
`secretsmanager:GetSecretValue`. No frontend GitHub SSH secrets or host variable
are needed.

The frontend is hosted by an Amazon Linux EC2 instance (`t3.micro` by default)
behind the existing load balancer. Get its current address with
`terraform output -raw frontend_instance_public_ip` after applying Terraform.
Update the `host` and `known_hosts` secret fields whenever that instance is
replaced; its public address and SSH host keys can change.

The `frontend_ssh_cidr` Terraform variable defaults to `0.0.0.0/0` because
GitHub-hosted runner addresses change. Use a fixed runner and a narrower CIDR
where possible. Frontend assets are copied directly over SSH; no S3 bucket is
used.

Terraform looks up the EC2 key pair named by `frontend_key_pair_name` (default
`pro`); it does not create a key pair. Rotate any key previously exposed and
store the replacement private key in the Secrets Manager deployment secret
described above.

To rotate the exposed `pro.pem`, generate a new key pair outside the repository
and import only its public key into EC2:

```powershell
ssh-keygen -t ed25519 -C "hrms-frontend-deploy" -f "$HOME/.ssh/hrms-frontend-rotated" -N ""
aws ec2 import-key-pair --key-name hrms-frontend-rotated --public-key-material "fileb://$HOME/.ssh/hrms-frontend-rotated.pub" --region ap-south-1
$env:TF_VAR_frontend_key_pair_name = "hrms-frontend-rotated"
terraform plan
terraform apply
```

Review the plan: changing the key-pair name replaces only the frontend EC2
instance. After it completes, refresh the host and `ssh-keyscan` output and run
the Secrets Manager helper with the replacement private key.

If Terraform reports that `hrms/application` is scheduled for deletion, restore
and import the existing secret before applying. With the AWS CLI configured
for the Terraform region, run:

```powershell
aws secretsmanager restore-secret --secret-id hrms/application --region ap-south-1
terraform import aws_secretsmanager_secret.application hrms/application
terraform apply
```

The import records the restored secret in Terraform state; apply then updates
its version using the currently supplied `TF_VAR_...` values.
