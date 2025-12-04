# Petshop - Script de Gerenciamento (Windows PowerShell)
# ABORDAGEM HÍBRIDA:
#   - Docker: Frontend, Spring Boot, ASP.NET, Java Functions (6)
#   - Local: C# Functions via func start (6)
param(
    [Parameter(Position=0)]
    [string]$Command = "start"
)

$ErrorActionPreference = "Continue"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$FunctionsDir = Join-Path $ScriptDir "functions"

# IMPORTANTE: Recarregar PATH do sistema para encontrar 'func' e outras ferramentas
$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")

function Print-Header {
    Write-Host ""
    Write-Host "=== PETSHOP - SISTEMA COMPLETO ===" -ForegroundColor Blue
    Write-Host "Frontend + 2 Backends + 12 Microsservicos (6 C# + 6 Java)" -ForegroundColor Blue
    Write-Host ""
}

function Print-Services {
    Write-Host "SERVICOS DISPONIVEIS:" -ForegroundColor Green
    Write-Host "  Frontend:        http://localhost:80" -ForegroundColor Green
    Write-Host "  Spring Boot:     http://localhost:8080" -ForegroundColor Green
    Write-Host "  ASP.NET:         http://localhost:5000" -ForegroundColor Green
    Write-Host ""
    Write-Host "  C# Functions (local):" -ForegroundColor Cyan
    Write-Host "    Auth:          http://localhost:7071" -ForegroundColor Green
    Write-Host "    Customers:     http://localhost:7072" -ForegroundColor Green
    Write-Host "    Pets:          http://localhost:7073" -ForegroundColor Green
    Write-Host "    Catalog:       http://localhost:7074" -ForegroundColor Green
    Write-Host "    Scheduling:    http://localhost:7075" -ForegroundColor Green
    Write-Host "    Orders:        http://localhost:7076" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Java Functions (Docker):" -ForegroundColor Cyan
    Write-Host "    Auth:          http://localhost:7081" -ForegroundColor Green
    Write-Host "    Customers:     http://localhost:7082" -ForegroundColor Green
    Write-Host "    Pets:          http://localhost:7083" -ForegroundColor Green
    Write-Host "    Catalog:       http://localhost:7084" -ForegroundColor Green
    Write-Host "    Scheduling:    http://localhost:7085" -ForegroundColor Green
    Write-Host "    Orders:        http://localhost:7086" -ForegroundColor Green
    Write-Host ""
}

function Start-DockerServices {
    Write-Host "Iniciando Docker (Frontend + Backends + Java Functions)..." -ForegroundColor Cyan
    Push-Location $ScriptDir
    # Inicia TODOS os serviços Docker incluindo Java Functions
    docker-compose up -d --build 2>$null
    Pop-Location
    Write-Host "Docker iniciado" -ForegroundColor Green
}

function Start-CSharpFunctions {
    Write-Host "Iniciando Azure Functions C#..." -ForegroundColor Cyan
    $startScript = Join-Path $FunctionsDir "start-all.ps1"
    if (Test-Path $startScript) {
        Push-Location $FunctionsDir
        & $startScript -SkipBuild
        Pop-Location
    }
}

function Stop-AllServices {
    Write-Host "Parando todos os servicos..." -ForegroundColor Yellow
    Push-Location $ScriptDir
    docker-compose down 2>$null
    Pop-Location
    $stopScript = Join-Path $FunctionsDir "stop-all.ps1"
    if (Test-Path $stopScript) {
        Push-Location $FunctionsDir
        & $stopScript
        Pop-Location
    }
    Write-Host "Servicos parados" -ForegroundColor Green
}

function Show-Status {
    Write-Host "STATUS DOS SERVICOS" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Docker Containers:" -ForegroundColor Yellow
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>$null
    Write-Host ""
    Write-Host "Testando endpoints..." -ForegroundColor Yellow
    $endpoints = @(
        @{Name="Frontend"; Url="http://localhost:80"},
        @{Name="Spring Boot"; Url="http://localhost:8080/api/produtos"},
        @{Name="ASP.NET"; Url="http://localhost:5000/api/produtos"},
        @{Name="Java Auth"; Url="http://localhost:7081/api/auth/health"},
        @{Name="Java Catalog"; Url="http://localhost:7084/api/produtos"}
    )
    foreach ($ep in $endpoints) {
        try {
            $r = Invoke-WebRequest -Uri $ep.Url -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
            Write-Host "  OK $($ep.Name)" -ForegroundColor Green
        } catch {
            Write-Host "  OFFLINE $($ep.Name)" -ForegroundColor Red
        }
    }
}

# Main
Print-Header
switch ($Command) {
    "start" { Start-DockerServices; Start-CSharpFunctions; Print-Services }
    "stop" { Stop-AllServices }
    "status" { Show-Status }
    "docker" { Start-DockerServices }
    "functions" { Start-CSharpFunctions }
    default { Start-DockerServices; Start-CSharpFunctions; Print-Services }
}
