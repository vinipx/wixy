---
sidebar_position: 1
title: Quick Start
---

# Quick Start Guide

Get WIXY running in under a minute. This guide covers installation, first launch, and creating your first stub.

## Prerequisites

| Requirement | Version | Purpose |
|-------------|---------|---------|
| **Java** | 21+ (LTS) | Runtime |
| **Gradle** | 9.x (bundled) | Build system |
| **Docker** | 24+ (optional) | Container deployment |

## Option 1 — Local Start (Recommended)

```bash
# Clone the repository
git clone https://github.com/vinipx/wixy.git
cd wixy

# Start locally (builds and runs with local profile)
./scripts/start-local.sh
```

This will:
1. Build the project (skipping tests for speed)
2. Start Spring Boot on **port 8080** (Admin API + Actuator)
3. Start embedded WireMock on **port 9090** (Stub Server)

## Option 2 — Docker Start

```bash
# Clone and start with Docker
git clone https://github.com/vinipx/wixy.git
cd wixy

# Build and run Docker container
./scripts/start-docker.sh

# Or use Docker Compose (includes proxy config)
docker-compose up
```

## Option 3 — Gradle Direct

```bash
# Build the project
./gradlew build

# Run with default profile
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Verify Installation

Once WIXY is running, verify all endpoints are accessible:

```bash
# Health check — should return {"status":"UP"}
curl http://localhost:8080/actuator/health

# List pre-packaged stubs
curl http://localhost:8080/wixy/admin/mappings

# Hit the sample stub
curl http://localhost:9090/api/sample

# Open Swagger UI in your browser
open http://localhost:8080/swagger-ui.html
```

## Create Your First Stub

### Step 1 — Define the Stub

```bash
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

### Step 2 — Hit the Stub

```bash
curl http://localhost:9090/api/users/1
```

**Response:**

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

### Step 3 — Verify It's Listed

```bash
curl http://localhost:8080/wixy/admin/mappings
```

### Step 4 — Clean Up

```bash
# Delete a specific stub by ID
curl -X DELETE http://localhost:8080/wixy/admin/mappings/{id}

# Or reset all stubs
curl -X POST http://localhost:8080/wixy/admin/mappings/reset
```

## Port Reference

| Port | Service | Purpose |
|------|---------|---------|
| `8080` | Spring Boot | Admin API, Actuator, Swagger UI |
| `9090` | WireMock | Stub server — send test traffic here |

:::tip
Both ports are fully configurable via environment variables. See the [Configuration guide](/docs/configuration/profiles) for details.
:::

## What's Next?

- **[Architecture Overview](/docs/architecture/overview)** — Understand how WIXY components interact
- **[Stub Management](/docs/features/stub-management)** — Deep dive into stub CRUD operations
- **[Proxy Mode](/docs/features/proxy-mode)** — Forward traffic to upstream services
- **[Recording](/docs/features/recording)** — Capture and replay real traffic
- **[Testing](/docs/testing/overview)** — Unit tests, integration tests, and coverage strategy
- **[Running Locally](/docs/testing/running-locally)** — All methods for running and testing locally
- **[API Reference](/docs/api/rest-endpoints)** — Complete endpoint documentation
