# Stop all Java Azure Functions for Petshop (Windows PowerShell)

$ErrorActionPreference = "SilentlyContinue"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Stopping Petshop Java Azure Functions" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

# Stop all background jobs
$jobs = Get-Job
if ($jobs) {
    Write-Host "Stopping $($jobs.Count) background jobs..." -ForegroundColor Yellow
    $jobs | Stop-Job
    $jobs | Remove-Job
    Write-Host "✅ Background jobs stopped" -ForegroundColor Green
}

# Kill any func processes on our ports
$ports = @(7081, 7082, 7083, 7084, 7085, 7086)
foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    foreach ($conn in $connections) {
        $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host "Stopping process $($proc.ProcessName) (PID: $($proc.Id)) on port $port" -ForegroundColor Yellow
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        }
    }
}

# Clean up pid files
$pidsDir = Join-Path $ScriptDir ".pids"
if (Test-Path $pidsDir) {
    Remove-Item -Path "$pidsDir\*" -Force -ErrorAction SilentlyContinue
    Write-Host "✅ Cleaned up PID files" -ForegroundColor Green
}

Write-Host ""
Write-Host "✅ All Java Functions stopped" -ForegroundColor Green
