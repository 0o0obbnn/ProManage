@echo off
echo 运行ProManage单元测试
echo.

echo 检查Java环境...
java -version
if %ERRORLEVEL% neq 0 (
    echo 错误: 未找到Java环境，请确保已安装Java并配置JAVA_HOME
    pause
    exit /b 1
)

echo.
echo 检查Maven环境...
call mvn -version
if %ERRORLEVEL% neq 0 (
    echo 警告: 未找到Maven，尝试使用Maven Wrapper...
    if exist "mvnw" (
        echo 使用Maven Wrapper运行测试
        call mvnw test
    ) else (
        echo 错误: 未找到Maven或Maven Wrapper
        echo 请安装Maven或使用IDE运行测试
        pause
        exit /b 1
    )
) else (
    echo 使用Maven运行测试
    call mvn clean test
)

echo.
echo 测试完成
pause