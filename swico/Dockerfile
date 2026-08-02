# Bước 1: Build file JAR bằng Maven & Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml và tải dependencies trước để lưu cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn và thực hiện build (bỏ qua unit tests)
COPY src ./src
RUN mvn clean package -DskipTests

# Bước 2: Khởi chạy ứng dụng bằng JRE nhẹ
FROM eclipse-temurin:17-jre
WORKDIR /app

# Chỉ copy đúng file jar thực thi (tránh file .original)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Chạy ứng dụng và tiêm cổng PORT của Render (mặc định 8080 nếu chạy local)
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]