# Bước 1: Build file JAR bằng Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng Spring Boot
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Khai báo cổng ứng dụng (mặc định Spring Boot chạy 8080)
EXPOSE 8080

# Chạy ứng dụng và truyền biến PORT do Render cung cấp
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]