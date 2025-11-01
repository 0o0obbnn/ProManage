# ProManage API Dockerfile
# Version: 1.1.0

# 使用官方OpenJDK运行时作为基础镜像
FROM openjdk:21-jdk-slim

# 设置维护者信息
LABEL maintainer="ProManage Team" \
      description="ProManage API Service" \
      version="1.1.0"

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV TZ=Asia/Shanghai

# 设置工作目录
WORKDIR /app

# 复制JAR文件到容器中
COPY promanage.jar app.jar

# 复制数据库迁移脚本
COPY src/main/resources/db/migration/ /app/db/migration/

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]