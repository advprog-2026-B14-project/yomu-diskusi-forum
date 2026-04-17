# Menggunakan base image JDK 21 yang ringan (Alpine Linux)
FROM eclipse-temurin:21-jdk-alpine@sha256:bcc7ec7e8fef937ba9f01ee5f810361d722c6b5dbe19ac188ab7b25c1a4dd2c9

# Menentukan direktori kerja di dalam kontainer
WORKDIR /app

# Menyalin file JAR hasil build ke dalam kontainer
COPY build/libs/*SNAPSHOT.jar app.jar

# Perintah untuk menjalankan aplikasi saat kontainer dimulai
ENTRYPOINT ["java", "-jar", "app.jar"]
