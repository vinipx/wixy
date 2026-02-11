# Wixy — WireMock Proxy Server on Spring Boot

> A lightweight, configurable test proxy service that embeds [WireMock](https://wiremock.org/) inside a Spring Boot application.

## Quick Start

```bash
# Clone and start locally (requires Java 21+)
./scripts/start-local.sh

# Or with Docker
./scripts/start-docker.sh
```

**Ports:**
| Port | Purpose |
|------|---------|
| `8080` | Spring Boot Admin API + Actuator |
| `9090` | WireMock stub server |

## Features

- **Stub Management** — Full CRUD REST API for creating/managing HTTP stubs
- **Proxy Mode** — Forward unmatched requests to a configurable upstream
- **Record & Playback** — Capture real traffic and replay it later
- **Profile-based Config** — `local`, `docker`, `cloud` profiles with env-var overrides
- **Health Checks** — Spring Actuator with WireMock status details
- **Optional Security** — API-key header authentication for shared/cloud environments
- **OpenAPI Docs** — Swagger UI at `/swagger-ui.html`

## Build & Test

```bash
# Full build with unit + integration tests
./gradlew check

# Unit tests only
./gradlew test

# Integration tests only (local)
./gradlew integrationTest

# Integration tests against a remote instance
./gradlew integrationTest -Dwixy.test.base-url=https://wixy.example.com

# Generate coverage report
./gradlew jacocoTestReport
# View at build/reports/jacoco/test/html/index.html
```

## API Examples

### Create a stub
```bash
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/hello"
    },
    "response": {
      "status": 200,
      "jsonBody": { "message": "Hello from Wixy!" },
      "headers": { "Content-Type": "application/json" }
    }
  }'
```

### Hit the stub
```bash
curl http://localhost:9090/api/hello
# → {"message":"Hello from Wixy!"}
```

### List all stubs
```bash
curl http://localhost:8080/wixy/admin/mappings
```

### Health check
```bash
curl http://localhost:8080/actuator/health
```

## Configuration

Configuration is managed via Spring profiles and environment variables. See [PLAN.md](PLAN.md) for full details.

| Variable | Default | Description |
|----------|---------|-------------|
| `WIXY_WIREMOCK_PORT` | `9090` | WireMock server port |
| `WIXY_PROXY_ENABLED` | `false` | Enable proxy mode |
| `WIXY_PROXY_TARGET_URL` | (empty) | Upstream URL for proxy/recording |
| `WIXY_PROXY_RECORD` | `false` | Enable traffic recording |
| `WIXY_SECURITY_ENABLED` | `false` | Enable API-key authentication |
| `WIXY_SECURITY_API_KEY` | (empty) | Required API-key header value |

## Docker

```bash
# Build image
docker build -t wixy:latest .

# Run with docker-compose (includes proxy to jsonplaceholder)
docker-compose up
```

## Architecture

See [PLAN.md](PLAN.md) for the full architectural plan, testing strategy, and implementation phases.

## License

MIT
