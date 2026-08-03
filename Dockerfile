# 构建阶段：编译 Java 项目
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN apk add --no-cache maven && mvn clean package -Dmaven.test.skip=true

# 运行阶段：只保留 JRE 和 jar 包
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/my-shop-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]