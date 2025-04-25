# 第一阶段：构建阶段
FROM maven:3.5-jdk-8-alpine AS builder
# 配置Maven使用阿里云镜像
RUN mkdir -p /root/.m2 \
    && echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 \
https://maven.apache.org/xsd/settings-1.0.0.xsd"> \
    <mirrors> \
        <mirror> \
            <id>aliyunmaven</id> \
            <mirrorOf>central</mirrorOf> \
            <name>阿里云公共仓库</name> \
            <url>https://maven.aliyun.com/repository/public</url> \
        </mirror> \
    </mirrors> \
</settings>' > /root/.m2/settings.xml
# 继续其他构建步骤
WORKDIR /app
COPY pom.xml .
# 先只复制pom.xml并下载依赖，利用Docker缓存机制
RUN mvn dependency:go-offline
# 然后复制源代码并构建
COPY src ./src
# Build a release artifact.
RUN mvn package -DskipTests

# 第二阶段：运行阶段
FROM openjdk:8-jre-alpine
WORKDIR /app
# 只复制构建好的JAR包
COPY --from=builder /app/target/user-center-backend-0.0.1-SNAPSHOT.jar ./
ENTRYPOINT ["java", "-jar", "user-center-backend-0.0.1-SNAPSHOT.jar"]
CMD ["--spring.profiles.active=prod"]