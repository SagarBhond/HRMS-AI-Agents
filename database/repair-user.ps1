$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)

if (-not $env:MYSQL_USER) { $env:MYSQL_USER = "hrms" }
if (-not $env:MYSQL_PASSWORD) { throw "Set MYSQL_PASSWORD before repairing the existing volume." }
if (-not $env:MYSQL_DATABASE) { $env:MYSQL_DATABASE = "hrms_db" }

docker compose exec -T mysql mysql -uroot "-p$env:MYSQL_ROOT_PASSWORD" -e @"
CREATE USER IF NOT EXISTS '$env:MYSQL_USER'@'%' IDENTIFIED BY '$env:MYSQL_PASSWORD';
ALTER USER '$env:MYSQL_USER'@'%' IDENTIFIED BY '$env:MYSQL_PASSWORD';
GRANT ALL PRIVILEGES ON \`$env:MYSQL_DATABASE\`.* TO '$env:MYSQL_USER'@'%';
FLUSH PRIVILEGES;
"@
