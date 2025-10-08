@echo off
REM ProManage Backend Startup Script
REM This script starts the ProManage backend application

echo ================================================
echo    Starting ProManage Backend Application
echo ================================================
echo.

echo Checking environment...
echo Maven version:
call mvn --version
echo.

echo Java version:
call java -version
echo.

echo Starting Spring Boot application with dev profile...
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=dev

pause

