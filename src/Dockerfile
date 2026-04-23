# 1. Java 17 버전을 사용하겠다는 뜻
FROM openjdk:17-jdk-slim

# 2. 빌드된 jar 파일을 서버로 복사해
COPY build/libs/*.jar app.jar

# 3. 서버가 시작될 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]

LABEL authors="에코수민"