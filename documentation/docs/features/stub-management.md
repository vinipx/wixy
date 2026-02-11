---
sidebar_position: 1
title: Stub Management
---

# Stub Management

WIXY provides a full REST API for creating, reading, updating, and deleting HTTP stubs at runtime. Stubs define how WireMock responds to specific request patterns.

## How Stubs Work

A **stub mapping** consists of two parts:

1. **Request matcher** — Defines the HTTP method, URL pattern, headers, and/or body that a request must match
2. **Response definition** — Defines the status code, headers, and body to return

When WireMock receives a request on port `9090`, it checks all active stubs for a match. The first match wins and its response is returned.

## Pre-Packaged Stubs

WIXY ships with sample stubs in `src/main/resources/wiremock/mappings/`. These load automatically on startup:

```json title="src/main/resources/wiremock/mappings/sample-stub.json"
{
  "request": {
    "method": "GET",
    "urlPath": "/api/sample"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "jsonBody": {
      "message": "Hello from Wixy!",
      "source": "pre-packaged stub"
    }
  }
}
```

:::tip
Place your own `.json` files in the `wiremock/mappings/` directory to pre-load stubs on every startup.
:::

## Creating Stubs via API

### Simple GET Stub

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
        "name": "Jane Doe",
        "email": "jane@example.com"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'
```

### POST Stub with Request Body Matching

```bash
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "POST",
      "urlPath": "/api/orders",
      "bodyPatterns": [
        { "matchesJsonPath": "$.productId" }
      ]
    },
    "response": {
      "status": 201,
      "jsonBody": {
        "orderId": "ORD-12345",
        "status": "CREATED"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'
```

### Stub with URL Pattern Matching

```bash
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPathPattern": "/api/products/[0-9]+"
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "id": 999,
        "name": "Generic Product"
      },
      "headers": {
        "Content-Type": "application/json"
      }
    }
  }'
```

### Stub with Delay (Simulating Latency)

```bash
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/slow-endpoint"
    },
    "response": {
      "status": 200,
      "jsonBody": { "message": "Delayed response" },
      "fixedDelayMilliseconds": 3000
    }
  }'
```

### Stub Returning Error Status

```bash
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/failing-service"
    },
    "response": {
      "status": 503,
      "jsonBody": {
        "error": "Service Unavailable",
        "message": "Dependency is down"
      }
    }
  }'
```

## Listing Stubs

```bash
curl http://localhost:8080/wixy/admin/mappings
```

**Response:**

```json
{
  "mappings": ["..."],
  "meta": {
    "total": 3
  }
}
```

## Getting a Stub by ID

```bash
curl http://localhost:8080/wixy/admin/mappings/{uuid}
```

## Updating a Stub

```bash
curl -X PUT http://localhost:8080/wixy/admin/mappings/{uuid} \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/users/1"
    },
    "response": {
      "status": 200,
      "jsonBody": { "id": 1, "name": "Updated Name" }
    }
  }'
```

## Deleting a Stub

```bash
# Delete a specific stub
curl -X DELETE http://localhost:8080/wixy/admin/mappings/{uuid}

# Reset ALL stubs
curl -X POST http://localhost:8080/wixy/admin/mappings/reset
```

## Bulk Import

Import multiple stubs at once using WireMock's import format:

```bash
curl -X POST http://localhost:8080/wixy/admin/mappings/import \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/batch-endpoint"
    },
    "response": {
      "status": 200,
      "jsonBody": { "batch": true }
    }
  }'
```

## Error Handling

| Scenario | HTTP Status | Error |
|----------|-------------|-------|
| Stub not found (GET/PUT/DELETE by ID) | `404` | `StubNotFoundException` |
| Invalid JSON in request body | `400` | `InvalidStubDefinitionException` |
| Malformed stub definition | `400` | `InvalidStubDefinitionException` |
