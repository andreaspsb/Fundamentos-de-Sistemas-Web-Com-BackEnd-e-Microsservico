# Start all C# Azure Functions for Petshop (Windows PowerShell)
param([switch]$SkipBuild)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# IMPORTANTE: Recarregar PATH do sistema para encontrar 'func' e outras ferramentas
$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")

Write-Host "==================================================="
Write-Host "  Starting Petshop C# Azure Functions (Windows)"
Write-Host "==================================================="

# Check prerequisites
$dotnetVersion = dotnet --version
Write-Host "OK .NET SDK: $dotnetVersion"

$funcVersion = func --version 2>$null
if (-not $funcVersion) {
    Write-Host "ERROR: Azure Functions Core Tools not installed"
    Write-Host "Install with: winget install Microsoft.Azure.FunctionsCoreTools"
    exit 1
}
Write-Host "OK Azure Functions: $funcVersion"

# Build shared library
if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "Building shared library..."
    Push-Location "Petshop.Shared"
    dotnet build -c Release
    Pop-Location
}

# Create directories
New-Item -ItemType Directory -Path "pids" -Force | Out-Null
New-Item -ItemType Directory -Path "logs" -Force | Out-Null

# Functions to start
$functions = @(
    @{Name="func-petshop-auth"; Port=7071},
    @{Name="func-petshop-customers"; Port=7072},
    @{Name="func-petshop-pets"; Port=7073},
    @{Name="func-petshop-catalog"; Port=7074},
    @{Name="func-petshop-scheduling"; Port=7075},
    @{Name="func-petshop-orders"; Port=7076}
)

# Build and start each function
foreach ($f in $functions) {
    $name = $f.Name
    $port = $f.Port
    $dir = Join-Path $ScriptDir $name
    
    if (-not $SkipBuild) {
        Write-Host ""
        Write-Host "Building $name..."
        Push-Location $dir
        dotnet build -c Release
        Pop-Location
    }
    
    Write-Host "Starting $name on port $port..."
    $logFile = Join-Path $ScriptDir "logs\$name.log"
    
    Start-Process -FilePath "func" -ArgumentList "start","--port",$port -WorkingDirectory $dir -RedirectStandardOutput $logFile -RedirectStandardError "$logFile.err" -WindowStyle Hidden
    
    Start-Sleep -Seconds 2
    Write-Host "  Started $name"
}

Write-Host ""
Write-Host "==================================================="
Write-Host "  All C# Functions Started!"
Write-Host "==================================================="
Write-Host ""
Write-Host "Endpoints:"
foreach ($f in $functions) {
    Write-Host "  http://localhost:$($f.Port)/api/"
}
Write-Host ""
Write-Host "Logs in: $ScriptDir\logs"
Write-Host "Stop with: .\stop-all.ps1"
