# 빌드 스테이지
FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build --no-daemon -x test && ls -la /app/build/libs/

# 실행 스테이지
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/job-world.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]