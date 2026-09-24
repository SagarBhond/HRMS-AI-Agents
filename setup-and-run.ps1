$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot
$envFile = Join-Path $PSScriptRoot ".env"

function New-Secret([int] $length = 32) {
    $bytes = New-Object byte[] $length
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    return ([Convert]::ToBase64String($bytes) -replace '[^A-Za-z0-9]', '').Substring(0, $length)
}

if (-not (Test-Path $envFile)) {
    $googleKey = Read-Host "GOOGLE_API_KEY (leave blank only if your services do not call Google)"
    $mysqlPassword = New-Secret
    $mysqlRootPassword = New-Secret
    $jwtSecret = New-Secret
    @"
MYSQL_DATABASE=hrms_db
MYSQL_USER=hrms
MYSQL_PASSWORD=$mysqlPassword
MYSQL_ROOT_PASSWORD=$mysqlRootPassword
JWT_SECRET=$jwtSecret
GOOGLE_API_KEY=$googleKey
"@ | Set-Content -Path $envFile -Encoding utf8
    Write-Host "Created .env with generated local database and JWT secrets."
}

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
    }
}

& (Join-Path $PSScriptRoot "run-stack.ps1")

if ($env:REPAIR_EXISTING_DB -eq "1") {
    & (Join-Path $PSScriptRoot "database\repair-user.ps1")
}
