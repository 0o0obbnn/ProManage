@echo off
echo ========================================
echo ProManage 回归测试运行脚本
echo ========================================
echo.

REM 设置环境变量
set MAVEN_OPTS=-Xmx2g -XX:MaxMetaspaceSize=512m
set JAVA_OPTS=-Dspring.profiles.active=test

echo 1. 清理并编译项目...
call mvn clean compile -q
if %ERRORLEVEL% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo 2. 运行单元测试...
call mvn test -Dtest="*Test" -q
if %ERRORLEVEL% neq 0 (
    echo 单元测试失败！
    pause
    exit /b 1
)

echo 3. 运行集成测试...
call mvn test -Dtest="*IntegrationTest" -q
if %ERRORLEVEL% neq 0 (
    echo 集成测试失败！
    pause
    exit /b 1
)

echo 4. 运行性能测试...
call mvn test -Dtest="*PerformanceTest" -q
if %ERRORLEVEL% neq 0 (
    echo 性能测试失败！
    pause
    exit /b 1
)

echo 5. 运行端到端测试...
call mvn test -Dtest="*EndToEndTest" -q
if %ERRORLEVEL% neq 0 (
    echo 端到端测试失败！
    pause
    exit /b 1
)

echo 6. 生成测试报告...
call mvn surefire-report:report -q

echo.
echo ========================================
echo 所有回归测试完成！
echo 测试报告位置: target/site/surefire-report.html
echo ========================================
pause
