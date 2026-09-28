[CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = 'High')]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$InstanceHost,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$PrivateKeyPath,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$KnownHostsPath,

    [ValidateNotNullOrEmpty()]
    [string]$Username = 'ec2-user',

    [ValidateNotNullOrEmpty()]
    [string]$Region = 'ap-south-1',

    [ValidateNotNullOrEmpty()]
    [string]$SecretId = 'hrms/frontend-deploy'
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $PrivateKeyPath -PathType Leaf)) {
    throw "Private key file not found: $PrivateKeyPath"
}

if (-not (Test-Path -LiteralPath $KnownHostsPath -PathType Leaf)) {
    throw "Known-hosts file not found: $KnownHostsPath"
}

if ($InstanceHost -notmatch '^[A-Za-z0-9.-]+$') {
    throw 'InstanceHost must be an EC2 public DNS name or IP address.'
}

$privateKey = [System.IO.File]::ReadAllText((Resolve-Path -LiteralPath $PrivateKeyPath).Path)
$privateKey = [regex]::Replace($privateKey, "`r`n?", "`n").TrimEnd()
$knownHosts = [System.IO.File]::ReadAllText((Resolve-Path -LiteralPath $KnownHostsPath).Path)
$knownHosts = [regex]::Replace($knownHosts, "`r`n?", "`n").Trim()

if ($privateKey -notmatch '(?m)^-----BEGIN (OPENSSH|RSA|EC) PRIVATE KEY-----$') {
    throw 'The private key file does not contain a supported PEM/OpenSSH private key.'
}

if (-not ($knownHosts -split '\r?\n' | Where-Object { $_ -match '\s(ssh-ed25519|ssh-rsa|ecdsa-sha2-nistp\d+)\s+[A-Za-z0-9+/]+' })) {
    throw 'The known-hosts file contains no SSH host-key line.'
}

$payload = [ordered]@{
    host        = $InstanceHost
    username    = $Username
    private_key = $privateKey
    known_hosts = $knownHosts
} | ConvertTo-Json -Depth 3 -Compress

$temporaryPath = Join-Path $env:TEMP "hrms-frontend-deploy-$([guid]::NewGuid().ToString('N')).json"

try {
    $file = [System.IO.File]::Open(
        $temporaryPath,
        [System.IO.FileMode]::CreateNew,
        [System.IO.FileAccess]::Write,
        [System.IO.FileShare]::None
    )
    $file.Dispose()

    $currentUser = [System.Security.Principal.WindowsIdentity]::GetCurrent().User
    $access = New-Object System.Security.AccessControl.FileSecurity
    $access.SetAccessRuleProtection($true, $false)
    $rule = New-Object System.Security.AccessControl.FileSystemAccessRule(
        $currentUser,
        [System.Security.AccessControl.FileSystemRights]::FullControl,
        [System.Security.AccessControl.AccessControlType]::Allow
    )
    $access.SetAccessRule($rule)
    Set-Acl -LiteralPath $temporaryPath -AclObject $access
    [System.IO.File]::WriteAllText($temporaryPath, $payload, (New-Object System.Text.UTF8Encoding($false)))

    if ($PSCmdlet.ShouldProcess($SecretId, 'Upload frontend deployment credentials to AWS Secrets Manager')) {
        & aws secretsmanager put-secret-value `
            --secret-id $SecretId `
            --secret-string "file://$temporaryPath" `
            --region $Region

        if ($LASTEXITCODE -ne 0) {
            throw "AWS CLI failed to update $SecretId."
        }

        Write-Output "Updated $SecretId in $Region."
    }
}
finally {
    if (Test-Path -LiteralPath $temporaryPath) {
        Remove-Item -LiteralPath $temporaryPath -Force
    }
}