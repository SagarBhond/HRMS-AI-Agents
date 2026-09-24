$ErrorActionPreference = "Stop"

$backend = $PSScriptRoot
$frontend = Join-Path (Split-Path -Parent $backend) "ShrijaAI_Model_Frontend-main"
$dockerCli = Join-Path $env:LOCALAPPDATA "Programs\DockerDesktop\resources\bin"
if (Test-Path (Join-Path $dockerCli "docker.exe")) {
    $env:Path = "$dockerCli;$env:Path"
}

Set-Location $backend
$envFile = Join-Path $backend ".env"
if (-not (Test-Path $envFile)) {
    Write-Warning "No .env file found; GOOGLE_API_KEY must be set in the shell environment."
}
$services = @("mysql", "mcp", "auth", "employee", "attendance", "payroll", "manager", "leave", "hr", "document", "notification", "policy", "expense", "asset", "performance", "recruitment", "audit", "compliance", "workflow", "backend")
foreach ($service in $services) {
    Write-Host "Building $service..."
    if ($service -eq "payroll") {
        & .\mvnw.cmd -B -ntp -pl payroll-agent -am package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build failed for payroll"
        }
        continue
    }
    docker compose --env-file $envFile build $service
    if ($LASTEXITCODE -ne 0) {
        throw "Docker build failed for $service"
    }
}
docker compose --env-file $envFile up -d --remove-orphans

Set-Location $frontend
docker compose up -d --build

docker compose -f (Join-Path $backend "docker-compose.yml") ps
docker compose ps
