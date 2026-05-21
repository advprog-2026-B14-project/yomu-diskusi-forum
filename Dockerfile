FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew && ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar
RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
