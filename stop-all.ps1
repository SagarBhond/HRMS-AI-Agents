$ErrorActionPreference = "Stop"

$backend = $PSScriptRoot
$frontend = Join-Path (Split-Path -Parent $backend) "ShrijaAI_Model_Frontend-main"

Set-Location $frontend
docker compose down --remove-orphans

Set-Location $backend
docker compose down --remove-orphans
