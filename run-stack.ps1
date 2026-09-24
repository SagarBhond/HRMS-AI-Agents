$ErrorActionPreference = "Stop"

$dockerCli = Join-Path $env:LOCALAPPDATA "Programs\DockerDesktop\resources\bin"
if (Test-Path (Join-Path $dockerCli "docker.exe")) {
    $env:Path = "$dockerCli;$env:Path"
}

Set-Location $PSScriptRoot
$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    throw "Missing .env. Copy .env.example to .env and set local secrets."
}

docker compose --env-file $envFile up -d mysql
docker compose --env-file $envFile up -d mcp auth
docker compose --env-file $envFile up -d employee leave document notification
docker compose --env-file $envFile up -d backend

$frontend = Join-Path (Split-Path -Parent $PSScriptRoot) "ShrijaAI_Model_Frontend-main"
docker compose -f (Join-Path $frontend "docker-compose.yml") up -d --build
docker compose ps
