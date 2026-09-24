$ErrorActionPreference = "Stop"
$dockerCli = Join-Path $env:LOCALAPPDATA "Programs\DockerDesktop\resources\bin"
if (Test-Path (Join-Path $dockerCli "docker.exe")) {
    $env:Path = "$dockerCli;$env:Path"
}
Set-Location $PSScriptRoot
docker compose up -d --build
docker compose ps
