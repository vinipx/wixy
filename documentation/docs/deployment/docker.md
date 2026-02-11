---
sidebar_position: 1
title: Docker
---

# Docker Deployment

WIXY ships with a production-ready multi-stage Dockerfile and Docker Compose configuration.

## Dockerfile

```dockerfile title="Dockerfile"
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
```

**Build the image:**

```bash
docker build -t wixy:latest .
```

## Docker Compose

```yaml title="docker-compose.yml"
version: "3.9"
services:
  wixy:
    build: .
    ports:
      - "8080:8080"
      - "9090:9090"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      WIXY_PROXY_TARGET_URL: "https://jsonplaceholder.typicode.com"
      WIXY_PROXY_ENABLED: "true"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 15s
```

**Start:**

```bash
docker-compose up -d
```

## Configuration via Environment

Pass any WIXY configuration as environment variables:

```bash
docker run -d \
  --name wixy \
  -p 8080:8080 \
  -p 9090:9090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e WIXY_PROXY_ENABLED=true \
  -e WIXY_PROXY_TARGET_URL=https://api.example.com \
  -e WIXY_PROXY_RECORD=true \
  -e WIXY_SECURITY_ENABLED=true \
  -e WIXY_SECURITY_API_KEY=my-secret-key \
  wixy:latest
```

## Custom Stubs via Volume Mount

Mount your own stub files into the container:

```bash
docker run -d \
  --name wixy \
  -p 8080:8080 \
  -p 9090:9090 \
  -v $(pwd)/my-stubs:/app/wiremock/mappings \
  wixy:latest
```

## Resource Limits

```yaml title="docker-compose.yml"
services:
  wixy:
    build: .
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M
```

## Networking

```yaml title="docker-compose.yml (with app under test)"
services:
  wixy:
    build: .
    ports:
      - "9090:9090"
    networks:
      - test-net

  app-under-test:
    image: my-app:latest
    environment:
      EXTERNAL_API_URL: http://wixy:9090
    networks:
      - test-net
    depends_on:
      wixy:
        condition: service_healthy

networks:
  test-net:
```
