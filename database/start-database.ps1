$ErrorActionPreference = "Stop"
$backend = Split-Path -Parent $PSScriptRoot
Set-Location $backend
docker compose up -d mysql
docker compose ps mysql
