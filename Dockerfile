# ── Build stage ──
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew bootJar -x test -x integrationTest --no-daemon

# ── Runtime stage ──
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/wixy-*.jar app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
