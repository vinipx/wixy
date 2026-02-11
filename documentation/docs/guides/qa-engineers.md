---
sidebar_position: 1
title: QA Engineers
---

# Guide for QA Engineers

This guide helps QA engineers use WIXY to stub external dependencies during integration, contract, and end-to-end testing.

## Why Use WIXY for Testing?

| Challenge | WIXY Solution |
|-----------|--------------|
| External services are unavailable or unstable | Stub them with predictable responses |
| Need to test error scenarios (timeouts, 500s) | Create stubs that return errors or delays |
| Tests are slow due to real network calls | Stubs respond instantly |
| Can't test edge cases against production APIs | Create custom responses for any scenario |
| Need consistent test data across runs | Stubs are deterministic and repeatable |

## Setting Up Your Test Environment

### 1. Start WIXY

```bash
# Using the startup script
./scripts/start-local.sh

# Or with Docker
docker-compose up -d
```

### 2. Create Stubs for Your Dependencies

**Example: Stubbing a User Service**

```bash
# Successful response
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {"method": "GET", "urlPath": "/api/users/1"},
    "response": {
      "status": 200,
      "jsonBody": {"id": 1, "name": "Test User", "role": "admin"},
      "headers": {"Content-Type": "application/json"}
    }
  }'

# User not found
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {"method": "GET", "urlPath": "/api/users/999"},
    "response": {
      "status": 404,
      "jsonBody": {"error": "User not found"},
      "headers": {"Content-Type": "application/json"}
    }
  }'

# Service timeout simulation
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {"method": "GET", "urlPath": "/api/users/slow"},
    "response": {
      "status": 200,
      "jsonBody": {"id": 2, "name": "Slow User"},
      "fixedDelayMilliseconds": 5000
    }
  }'
```

### 3. Point Your Application Under Test to WIXY

Configure your application to use `http://localhost:9090` as the base URL for the stubbed service.

### 4. Run Your Tests

Your tests now hit WIXY instead of the real service, giving you full control over responses.

### 5. Clean Up Between Test Suites

```bash
curl -X POST http://localhost:8080/wixy/admin/mappings/reset
```

## Common Testing Patterns

### Contract Testing

Record a real service's responses, then validate your application against the recordings:

```bash
# 1. Record from real service
curl -X POST http://localhost:8080/wixy/admin/recordings/start \
  -d '{"targetUrl": "https://real-api.example.com"}'

# 2. Exercise your application's flows
# ... run your test suite ...

# 3. Stop recording
curl -X POST http://localhost:8080/wixy/admin/recordings/stop

# 4. Future runs use recorded stubs — no real service needed
```

### Error Scenario Testing

```bash
# 500 Internal Server Error
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {"method": "POST", "urlPath": "/api/orders"},
    "response": {"status": 500, "jsonBody": {"error": "Internal Server Error"}}
  }'

# 429 Rate Limited
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {"method": "GET", "urlPathPattern": "/api/.*"},
    "response": {"status": 429, "jsonBody": {"error": "Rate limit exceeded"}}
  }'
```

### Data-Driven Testing

Create stubs with URL patterns to serve different responses:

```bash
# Pattern-based stub
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPathPattern": "/api/products/[0-9]+"
    },
    "response": {
      "status": 200,
      "jsonBody": {"id": 1, "name": "Generic Product", "price": 29.99}
    }
  }'
```

## Tips for QA Engineers

:::tip Best Practices
- **Reset stubs between test suites** to avoid cross-contamination
- **Use file-based stubs** for common fixtures that don't change
- **Use API-created stubs** for test-specific scenarios
- **Record once, replay many** — use Record & Playback for contract testing
- **Check the Swagger UI** at `/swagger-ui.html` for interactive API exploration
:::
