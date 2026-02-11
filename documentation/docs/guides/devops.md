---
sidebar_position: 3
title: DevOps
---

# Guide for DevOps Engineers

This guide covers deploying WIXY in containerised and cloud environments, monitoring, and CI/CD integration.

## Docker Deployment

### Build the Image

```bash
docker build -t wixy:latest .
```

The multi-stage Dockerfile produces a minimal image:

```dockerfile title="Dockerfile"
# Build stage — compiles with JDK 21
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew bootJar -x test -x integrationTest --no-daemon

# Runtime stage — runs with JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/wixy-*.jar app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Run with Docker

```bash
docker run -d \
  --name wixy \
  -p 8080:8080 \
  -p 9090:9090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  wixy:latest
```

### Docker Compose

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

## Kubernetes Deployment

See the dedicated [Kubernetes guide](/docs/deployment/kubernetes) for full manifests.

### Health Probes

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 20
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

## Monitoring

### Exposed Actuator Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Application + WireMock health status |
| `/actuator/info` | Build and application info |
| `/actuator/metrics` | JVM, HTTP, and custom metrics |

### Key Metrics to Monitor

| Metric | Significance |
|--------|-------------|
| `wiremock.stubCount` | Number of active stub mappings |
| `http.server.requests` | Request rate and latency |
| `jvm.memory.used` | JVM heap usage |
| `system.cpu.usage` | CPU utilisation |

## Security in Production

| Concern | Mitigation |
|---------|-----------|
| API key in transit | TLS termination at load balancer/ingress |
| API key storage | Kubernetes Secrets, AWS SSM, or Vault |
| Unauthorized access | Enable `wixy.security.enabled=true` |
| Network exposure | Internal network only, no public internet |

## Resource Requirements

### Recommended Sizing

| Environment | CPU | Memory | Stubs |
|-------------|-----|--------|-------|
| Development | 0.25 vCPU | 256 MB | < 100 |
| Testing | 0.5 vCPU | 512 MB | < 500 |
| Shared/CI | 1 vCPU | 1 GB | < 2000 |

### JVM Tuning

```bash
docker run -d \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  wixy:latest
```

## Operational Runbook

### Health Check Failed

```bash
# Check container logs
docker logs wixy

# Verify both ports are responding
curl http://localhost:8080/actuator/health
curl http://localhost:9090/__admin/mappings
```

### High Memory Usage

```bash
# Check stub count — large numbers increase memory
curl http://localhost:8080/wixy/admin/mappings | jq '.meta.total'

# Reset stubs if needed
curl -X POST http://localhost:8080/wixy/admin/mappings/reset
```

### WireMock Not Responding

```bash
# Check health indicator
curl http://localhost:8080/actuator/health | jq '.components.wiremock'

# Restart the container
docker restart wixy
```
