# ProManage Backend Startup Script
# This script starts the ProManage backend application

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   Starting ProManage Backend Application" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Check if port 8080 is in use
Write-Host "Checking if port 8080 is available..." -ForegroundColor Yellow
$portInUse = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "ERROR: Port 8080 is already in use!" -ForegroundColor Red
    Write-Host "Please stop the process using port 8080 and try again." -ForegroundColor Red
    exit 1
}
Write-Host "✓ Port 8080 is available" -ForegroundColor Green
Write-Host ""

# Start the application
Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
Write-Host "Profile: dev" -ForegroundColor Cyan
Write-Host ""

mvn spring-boot:run -Dspring-boot.run.profiles=dev

