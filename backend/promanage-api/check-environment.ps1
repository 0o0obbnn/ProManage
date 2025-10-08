# ProManage Environment Check Script
# This script checks if all required services are available

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   ProManage Environment Check" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✓ Java is installed: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java is not installed or not in PATH" -ForegroundColor Red
}
Write-Host ""

# Check Maven
Write-Host "Checking Maven..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn --version 2>&1 | Select-String "Apache Maven"
    Write-Host "✓ Maven is installed: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Maven is not installed or not in PATH" -ForegroundColor Red
}
Write-Host ""

# Check PostgreSQL connection
Write-Host "Checking PostgreSQL connection..." -ForegroundColor Yellow
try {
    $pgHost = "192.168.18.7"
    $pgPort = 5432
    $connection = Test-NetConnection -ComputerName $pgHost -Port $pgPort -WarningAction SilentlyContinue
    if ($connection.TcpTestSucceeded) {
        Write-Host "✓ PostgreSQL is accessible at ${pgHost}:${pgPort}" -ForegroundColor Green
    } else {
        Write-Host "✗ Cannot connect to PostgreSQL at ${pgHost}:${pgPort}" -ForegroundColor Red
        Write-Host "  Please ensure PostgreSQL is running and accessible" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ Error checking PostgreSQL connection" -ForegroundColor Red
}
Write-Host ""

# Check Redis connection
Write-Host "Checking Redis connection..." -ForegroundColor Yellow
try {
    $redisHost = "192.168.18.7"
    $redisPort = 6379
    $connection = Test-NetConnection -ComputerName $redisHost -Port $redisPort -WarningAction SilentlyContinue
    if ($connection.TcpTestSucceeded) {
        Write-Host "✓ Redis is accessible at ${redisHost}:${redisPort}" -ForegroundColor Green
    } else {
        Write-Host "✗ Cannot connect to Redis at ${redisHost}:${redisPort}" -ForegroundColor Red
        Write-Host "  Please ensure Redis is running and accessible" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ Error checking Redis connection" -ForegroundColor Red
}
Write-Host ""

# Check port 8080
Write-Host "Checking if port 8080 is available..." -ForegroundColor Yellow
$portInUse = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "✗ Port 8080 is already in use" -ForegroundColor Red
    Write-Host "  Process using port 8080:" -ForegroundColor Yellow
    Get-Process -Id $portInUse.OwningProcess | Format-Table Id, ProcessName, StartTime
} else {
    Write-Host "✓ Port 8080 is available" -ForegroundColor Green
}
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   Environment Check Complete" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "If all checks passed, you can start the application with:" -ForegroundColor Green
Write-Host "  .\start-app.bat" -ForegroundColor Cyan
Write-Host "  or" -ForegroundColor Green
Write-Host "  mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""

