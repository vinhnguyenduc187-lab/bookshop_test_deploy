# -------------------------
# Stage 1: Build ứng dụng
# -------------------------
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

# Sao chép file cấu hình và code
COPY . .

# Build file .jar (không chạy test để tiết kiệm thời gian)
RUN gradle clean bootJar --no-daemon -x test


# -------------------------
# Stage 2: Run ứng dụng
# -------------------------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Sao chép file JAR từ stage build
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port mà Spring Boot dùng
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java","-jar","app.jar"]
