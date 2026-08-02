# Bước 1: Build file JAR bằng Maven & Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy toàn bộ mã nguồn
COPY . .

# Giới hạn RAM cho Maven để tránh bị nổ bộ nhớ trên gói Free của Render
ENV MAVEN_OPTS="-Xmx400m"

# Build dự án (bỏ qua unit tests)
RUN mvn clean package -DskipTests

# Bước 2: Khởi chạy ứng dụng bằng JRE Java 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy file jar thực thi
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Chạy ứng dụng và tiêm cổng PORT từ Render
ENTRYPOINT ["sh", "-c", "java -Xmx300m -jar app.jar --server.port=${PORT:-8080}"]