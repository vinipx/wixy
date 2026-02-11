---
sidebar_position: 5
title: Health Monitoring
---

# Health Monitoring

WIXY integrates with Spring Boot Actuator to provide comprehensive health and operational status information, including a custom WireMock health indicator.

## Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Overall application health including WireMock status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Response:**

```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 234567890123,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    },
    "wiremock": {
      "status": "UP",
      "details": {
        "port": 9090,
        "stubCount": 3
      }
    }
  }
}
```

## Custom WireMock Health Indicator

The `WireMockHealthIndicator` reports:

| Detail | Description |
|--------|-------------|
| `port` | The port WireMock is listening on |
| `stubCount` | Number of active stub mappings |

**States:**

| State | Condition |
|-------|-----------|
| `UP` | WireMock server is running |
| `DOWN` | WireMock server is not running or null |

## Configuration

Health details are visible by default:

```yaml title="application.yml"
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## OpenAPI / Swagger UI

WIXY includes auto-generated API documentation:

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3.0 specification (JSON) |

The Swagger UI allows you to explore and test every Admin API endpoint directly from the browser.

## Monitoring in Production

For production/cloud deployments, use the health endpoint for:

- **Kubernetes liveness/readiness probes**
- **Load balancer health checks**
- **Monitoring dashboards** (Prometheus, Grafana, DataDog)

```yaml title="kubernetes-deployment.yml (excerpt)"
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 15
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 5
```
