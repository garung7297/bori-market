# 1. 렌더 서버 안에서 직접 빌드(Build)를 시작해
FROM gradle:7.6-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# gradlew에 실행 권한을 주고 빌드해 (테스트는 건너뛰어서 속도를 높여!)
RUN chmod +x gradlew
RUN ./gradlew build -x test

# 2. 빌드가 끝나면 실행(Run) 준비를 해 FROM openjdk:17-jdk-slim
# 2. 실행을 위한 환경 설정 (이 부분을 더 안정적인 걸로 바꿨어!)
FROM eclipse-temurin:17-jdk-alpine
EXPOSE 8080
# 위에서 만든 따끈따끈한 jar 파일을 가져와 COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
# 3. 드디어 서버 시작
ENTRYPOINT ["java", "-jar", "/app.jar"]