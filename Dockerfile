# Multi-stage build for Music Place Backend

# Stage 1: Build
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

# Gradle 캐시 최적화를 위해 의존성 먼저 다운로드
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 필요한 패키지 설치 (curl for healthcheck)
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV TZ=Asia/Seoul
ENV LANG=ko_KR.UTF-8
ENV LC_ALL=ko_KR.UTF-8
ENV LANGUAGE=ko_KR.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
# 포트 노출
EXPOSE 8080 8081

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
