---
sidebar_position: 4
title: Running Locally
---

# Running WIXY Locally for Testing

This guide covers every method for running WIXY on your local machine — for both manual exploration and automated test execution.

## Prerequisites

| Requirement | Version | Check Command |
|-------------|---------|---------------|
| **Java** | 21+ (LTS) | `java -version` |
| **Gradle** | 9.x (bundled via wrapper) | `./gradlew --version` |
| **Docker** | 24+ (optional) | `docker --version` |

## Method 1 — Gradle `bootRun` (Recommended for Development)

The fastest way to start WIXY locally with hot-reload capability:

```bash
# Start with the default profile
./gradlew bootRun

# Start with a specific profile
./gradlew bootRun --args='--spring.profiles.active=local'

# Start with custom configuration overrides
WIXY_WIREMOCK_PORT=9999 ./gradlew bootRun
```

**What happens:**
1. Gradle compiles the project
2. Spring Boot starts on **port 8080** (Admin API + Actuator)
3. Embedded WireMock starts on **port 9090** (Stub Server)
4. Pre-packaged stubs from `src/main/resources/wiremock/mappings/` are loaded

**Verify it's running:**

```bash
# Health check
curl http://localhost:8080/actuator/health
# → {"status":"UP","components":{"wiremock":{"status":"UP","details":{"port":9090,"stubCount":1}}}}

# List stubs (includes pre-packaged sample)
curl http://localhost:8080/wixy/admin/mappings
# → {"mappings":[...],"meta":{"total":1}}

# Hit the sample stub
curl http://localhost:9090/api/sample
# → {"message":"Hello from Wixy!","source":"pre-packaged stub"}

# Open Swagger UI in browser
open http://localhost:8080/swagger-ui.html
```

**Stop the server:** Press `Ctrl+C` in the terminal.

---

## Method 2 — Fat JAR (Recommended for Testing)

Build once, run anywhere:

```bash
# Build the JAR (skip tests for speed)
./gradlew bootJar -x test

# Run with the default profile
java -jar build/libs/wixy-0.1.0-SNAPSHOT.jar

# Run with a specific profile
java -jar build/libs/wixy-0.1.0-SNAPSHOT.jar \
    --spring.profiles.active=local

# Run with full customisation
java -jar build/libs/wixy-0.1.0-SNAPSHOT.jar \
    --spring.profiles.active=local \
    --wixy.wiremock.port=9091 \
    --wixy.proxy.enabled=true \
    --wixy.proxy.target-url=https://jsonplaceholder.typicode.com
```

---

## Method 3 — Docker (Recommended for CI/CD & Team Environments)

### Docker Build & Run

```bash
# Build the image
docker build -t wixy:latest .

# Run with default settings
docker run -d --name wixy \
    -p 8080:8080 \
    -p 9090:9090 \
    wixy:latest

# Run with proxy enabled
docker run -d --name wixy \
    -p 8080:8080 \
    -p 9090:9090 \
    -e SPRING_PROFILES_ACTIVE=docker \
    -e WIXY_PROXY_ENABLED=true \
    -e WIXY_PROXY_TARGET_URL=https://jsonplaceholder.typicode.com \
    wixy:latest

# Check logs
docker logs -f wixy

# Stop
docker stop wixy && docker rm wixy
```

### Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

---

## Method 4 — Startup Scripts

If available in the repository:

```bash
# Local (builds + runs with local profile)
./scripts/start-local.sh

# Docker (builds image + runs container)
./scripts/start-docker.sh
```

---

## Port Reference

| Port | Service | Purpose | Configurable Via |
|------|---------|---------|------------------|
| `8080` | Spring Boot | Admin API, Actuator, Swagger UI | `SERVER_PORT` |
| `9090` | WireMock | Stub server — send test traffic here | `WIXY_WIREMOCK_PORT` |

:::tip
If you have port conflicts, change them via environment variables:
```bash
SERVER_PORT=3000 WIXY_WIREMOCK_PORT=3001 ./gradlew bootRun
```
:::

---

## Running Tests Against a Local Instance

### Unit Tests (No Running Instance Needed)

Unit tests are **completely self-contained** — they mock all dependencies and never make network calls:

```bash
# Run all 129 unit tests (~3 seconds)
./gradlew test
```

### Integration Tests (Auto-Starts the Instance)

Integration tests automatically **start their own embedded WIXY instance** on a random port — you do NOT need to manually start the server first:

```bash
# Run all 57 integration tests (~15 seconds)
./gradlew integrationTest
```

### Integration Tests Against a Manually Running Instance

If you prefer to test against a running WIXY instance (e.g., one started via `bootRun` or Docker):

```bash
# Start WIXY in one terminal
./gradlew bootRun

# In another terminal, run integration tests against it
./gradlew integrationTest \
    -Dwixy.test.base-url=http://localhost:8080
```

### Full Build (Unit + Integration + Coverage)

```bash
# Run everything: compile, unit tests, integration tests, JaCoCo coverage
./gradlew check

# The build fails if coverage drops below 80%
```

### Generate and View Coverage Report

```bash
# Generate JaCoCo HTML report
./gradlew jacocoTestReport

# Open the report (macOS)
open build/reports/jacoco/test/html/index.html

# Or on Linux
xdg-open build/reports/jacoco/test/html/index.html
```

---

## Testing Workflow: Local Development

Here's a typical development workflow for working on WIXY locally:

### 1. Start the Server

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2. Create Test Stubs

```bash
# Create a user endpoint stub
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/users/1"
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'
```

### 3. Test the Stubs

```bash
# Send traffic to WireMock port
curl http://localhost:9090/api/users/1
# → {"id":1,"name":"John Doe","email":"john@example.com"}
```

### 4. Explore via Swagger UI

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser. You can:
- Try all Admin API endpoints interactively
- See request/response schemas
- Test create, read, update, delete operations

### 5. Clean Up

```bash
# Reset all runtime stubs
curl -X POST http://localhost:8080/wixy/admin/mappings/reset
```

---

## Testing Workflow: Proxy Mode

### 1. Start with Proxy Enabled

```bash
WIXY_PROXY_ENABLED=true \
WIXY_PROXY_TARGET_URL=https://jsonplaceholder.typicode.com \
./gradlew bootRun
```

### 2. Test Proxy Forwarding

```bash
# This gets proxied to the real jsonplaceholder API
curl http://localhost:9090/posts/1
# → Real response from jsonplaceholder

# Create a stub that overrides a specific endpoint
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {"method": "GET", "urlPath": "/posts/1"},
    "response": {"status": 200, "jsonBody": {"id": 1, "title": "Stubbed!"}}
  }'

# Now the stub takes priority
curl http://localhost:9090/posts/1
# → {"id":1,"title":"Stubbed!"}

# Other paths still proxy to the real API
curl http://localhost:9090/posts/2
# → Real response from jsonplaceholder
```

---

## Testing Workflow: Recording Mode

### 1. Start Recording

```bash
# Enable recording to capture real API traffic
curl -X POST http://localhost:8080/wixy/admin/recordings/start \
  -H "Content-Type: application/json" \
  -d '{"targetUrl": "https://jsonplaceholder.typicode.com"}'
```

### 2. Send Traffic Through WIXY

```bash
# These requests are forwarded AND recorded
curl http://localhost:9090/posts/1
curl http://localhost:9090/users/1
curl http://localhost:9090/todos/1
```

### 3. Stop Recording and Inspect

```bash
# Stop — shows how many stubs were captured
curl -X POST http://localhost:8080/wixy/admin/recordings/stop
# → {"status":"Recording stopped","capturedStubs":3}

# View the captured stubs
curl http://localhost:8080/wixy/admin/mappings
```

### 4. Replay Without the Real API

```bash
# These now return recorded responses — no network calls!
curl http://localhost:9090/posts/1
curl http://localhost:9090/users/1
```

---

## Troubleshooting Local Runs

### Port Already in Use

```bash
# Find what's using the port
lsof -i :8080
lsof -i :9090

# Kill the process
kill -9 <PID>

# Or use different ports
SERVER_PORT=3000 WIXY_WIREMOCK_PORT=3001 ./gradlew bootRun
```

### Java Version Issues

```bash
# Verify Java 21+
java -version

# If wrong version, set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Build Failures

```bash
# Clean rebuild with fresh dependencies
./gradlew clean build --refresh-dependencies

# Build without tests (faster)
./gradlew bootJar -x test
```

### Integration Tests Fail Against Remote

```bash
# Verify the remote instance is reachable
curl -sf https://your-wixy-instance.example.com/actuator/health

# Check if security is enabled (you may need an API key)
curl -H "X-Wixy-Api-Key: your-key" \
    https://your-wixy-instance.example.com/wixy/admin/mappings
```
