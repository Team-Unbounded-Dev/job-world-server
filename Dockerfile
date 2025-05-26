# 빌드 스테이지
FROM gradle:8.2-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# 실행 스테이지
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]