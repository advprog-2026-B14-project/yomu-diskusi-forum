FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY . .

# Increase Gradle download timeout to prevent 10s timeout on slow networks
ENV GRADLE_OPTS="-Dorg.gradle.internal.http.connectionTimeout=120000 -Dorg.gradle.internal.http.socketTimeout=120000"
RUN chmod +x gradlew && ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar
RUN apk add --no-cache curl

EXPOSE ${PORT:-8080}

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health/liveness || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-Xmx96m", \
  "-Xms64m", \
  "-XX:MaxMetaspaceSize=96m", \
  "-XX:+UseSerialGC", \
  "-XX:CICompilerCount=1", \
  "-XX:+TieredCompilation", \
  "-XX:TieredStopAtLevel=1", \
  "-Xss256k", \
  "-jar", "app.jar"]

