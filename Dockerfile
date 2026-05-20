FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew && ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
