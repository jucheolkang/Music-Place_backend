# ===========================================
# Stage 1 : Build
# ===========================================
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

# Gradle Cache
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

# Source Copy
COPY src ./src

# Build
RUN gradle clean build -x test --no-daemon

# ===========================================
# Stage 2 : Runtime
# ===========================================
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install packages
RUN apt-get update && \
    apt-get install -y curl wget && \
    rm -rf /var/lib/apt/lists/*

# Log Directory
RUN mkdir -p /app/logs

# Jar Copy
COPY --from=builder /app/build/libs/*.jar app.jar

# JVM Option
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENV TZ=Asia/Seoul
ENV LANG=ko_KR.UTF-8
ENV LC_ALL=ko_KR.UTF-8
ENV LANGUAGE=ko_KR.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

EXPOSE 8080
EXPOSE 8081

HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=60s \
            --retries=3 \
CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
