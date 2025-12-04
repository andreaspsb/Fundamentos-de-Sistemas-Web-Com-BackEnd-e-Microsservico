# Start all Java Azure Functions for Petshop (Windows PowerShell)
# This script builds and starts all Java function apps

param(
    [switch]$SkipBuild,
    [switch]$UseH2Server
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Starting Petshop Java Azure Functions (Windows)" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "✅ Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Java is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Check if Maven wrapper exists or Maven is installed
$mvnCmd = $null
if (Test-Path "mvnw.cmd") {
    $mvnCmd = ".\mvnw.cmd"
    Write-Host "✅ Using Maven Wrapper" -ForegroundColor Green
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvnCmd = "mvn"
    Write-Host "✅ Maven found in PATH" -ForegroundColor Green
} else {
    Write-Host "⚠️ Maven not found. Downloading Maven Wrapper..." -ForegroundColor Yellow
    
    # Download Maven Wrapper
    $wrapperUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper-distribution/3.2.0/maven-wrapper-distribution-3.2.0-bin.zip"
    $wrapperZip = "$env:TEMP\maven-wrapper.zip"
    
    Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperZip
    Expand-Archive -Path $wrapperZip -DestinationPath "$env:TEMP\maven-wrapper" -Force
    
    # Create mvnw.cmd
    @"
@echo off
set MAVEN_PROJECTBASEDIR=%~dp0
java -jar "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar" %*
"@ | Out-File -FilePath "mvnw.cmd" -Encoding ASCII
    
    # Create .mvn directory and download wrapper jar
    New-Item -ItemType Directory -Path ".mvn\wrapper" -Force | Out-Null
    $wrapperJarUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    Invoke-WebRequest -Uri $wrapperJarUrl -OutFile ".mvn\wrapper\maven-wrapper.jar"
    
    $mvnCmd = ".\mvnw.cmd"
    Write-Host "✅ Maven Wrapper downloaded" -ForegroundColor Green
}

# Check if Azure Functions Core Tools is installed
try {
    $funcVersion = func --version 2>&1
    Write-Host "✅ Azure Functions Core Tools: $funcVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Azure Functions Core Tools is not installed" -ForegroundColor Red
    Write-Host "   Install with: npm install -g azure-functions-core-tools@4" -ForegroundColor Yellow
    Write-Host "   Or: winget install Microsoft.AzureFunctionsCoreTools" -ForegroundColor Yellow
    exit 1
}

# Build the project
if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "🔨 Building all modules..." -ForegroundColor Yellow
    & $mvnCmd clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Build failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Build completed" -ForegroundColor Green
}

# Create pid directory
$pidsDir = Join-Path $ScriptDir ".pids"
New-Item -ItemType Directory -Path $pidsDir -Force | Out-Null

# Function definitions
$functions = @(
    @{ Name = "func-petshop-auth-java"; Port = 7081 },
    @{ Name = "func-petshop-customers-java"; Port = 7082 },
    @{ Name = "func-petshop-pets-java"; Port = 7083 },
    @{ Name = "func-petshop-catalog-java"; Port = 7084 },
    @{ Name = "func-petshop-scheduling-java"; Port = 7085 },
    @{ Name = "func-petshop-orders-java"; Port = 7086 }
)

# Start functions
$jobs = @()
foreach ($func in $functions) {
    $name = $func.Name
    $port = $func.Port
    $funcDir = Join-Path $ScriptDir "$name\target\azure-functions\$name"
    
    if (-not (Test-Path $funcDir)) {
        Write-Host "❌ Function directory not found: $funcDir" -ForegroundColor Red
        Write-Host "   Run build first without -SkipBuild" -ForegroundColor Yellow
        continue
    }
    
    Write-Host ""
    Write-Host "🚀 Starting $name on port $port..." -ForegroundColor Yellow
    
    $job = Start-Job -ScriptBlock {
        param($dir, $port)
        Set-Location $dir
        func start --port $port 2>&1
    } -ArgumentList $funcDir, $port
    
    $jobs += @{ Job = $job; Name = $name; Port = $port }
    
    # Save job ID to file
    $job.Id | Out-File -FilePath (Join-Path $pidsDir "$name.pid")
    
    Write-Host "   Started with Job ID: $($job.Id)" -ForegroundColor Green
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  All Java Functions Started!" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Endpoints:" -ForegroundColor Yellow
foreach ($func in $functions) {
    Write-Host "  $($func.Name): http://localhost:$($func.Port)/api/" -ForegroundColor White
}
Write-Host ""
Write-Host "To stop all functions, run: .\stop-all-java.ps1" -ForegroundColor Yellow
Write-Host "To view logs, run: Get-Job | Receive-Job" -ForegroundColor Yellow
Write-Host ""

# Keep script running and show logs
Write-Host "Press Ctrl+C to stop all functions..." -ForegroundColor Gray
Write-Host ""

try {
    while ($true) {
        foreach ($j in $jobs) {
            $output = Receive-Job -Job $j.Job -ErrorAction SilentlyContinue
            if ($output) {
                foreach ($line in $output) {
                    Write-Host "[$($j.Name)] $line"
                }
            }
        }
        Start-Sleep -Seconds 1
    }
} finally {
    Write-Host ""
    Write-Host "Stopping all functions..." -ForegroundColor Yellow
    Get-Job | Stop-Job
    Get-Job | Remove-Job
    Write-Host "✅ All functions stopped" -ForegroundColor Green
}
