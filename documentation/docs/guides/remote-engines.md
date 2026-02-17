---
sidebar_position: 4
title: Deploying Remote Engines
---

# Deploying Remote Engines

WIXY Hub is designed to manage a distributed fleet of WireMock servers. This guide covers how to deploy and configure remote engines in a microservices architecture.

## Deployment Patterns

### 1. The Shared Fleet (Centralized)
A set of standalone WireMock instances running in a shared environment (e.g., Kubernetes `dev` namespace). 
- **Best for**: Stable API contracts used by multiple teams.
- **Setup**: Deploy N instances of WIXY or standard WireMock.

### 2. The Sidecar Pattern (Service-Specific)
Every microservice in your stack gets its own dedicated "shadow" mock server running in the same Pod or on the same VM.
- **Best for**: Testing service-specific failure modes (timeouts, 500s) without affecting other teams.
- **Setup**: Run a mock container alongside your application container.

---

## Option A: Using WIXY as a Remote Node (Recommended)
Running WIXY as a remote engine provides the best experience, as it includes the Hub-compatible API and health checks.

### Lightweight "Headless" Mode
When running WIXY as a remote node, you usually don't need the Management UI. Disable it to save resources:

```bash
docker run -d 
  -p 8080:8080 
  -p 9090:9090 
  -e WIXY_UI_ENABLED=false 
  -e WIXY_WIREMOCK_VERBOSE=false 
  vinipx/wixy:latest
```

---

## Option B: Using Standard WireMock
WIXY Hub is 100% compatible with the official WireMock standalone server.

```bash
docker run -d 
  -p 8080:8080 
  wiremock/wiremock:latest
```

---

## 🚀 Orchestrating a Fleet (Docker Compose)

The most efficient way to manage multiple services is to define your "Mock Fleet" in a dedicated compose file. This allows you to spin up your entire testing infrastructure with one command.

### Example: Hub + Multi-Service Mocks
This configuration creates a centralized Hub and three specialized remote mocks for a typical E-commerce architecture.

```yaml title="docker-compose-hub.yml"
services:
  # The Central Controller
  wixy-hub:
    image: vinipx/wixy:latest
    ports: ["8080:8080"]
    environment:
      - WIXY_REGISTRY_FILE_PATH=/data/servers.json
    volumes:
      - ./hub-data:/data

  # Remote Mock 1: Payment Gateway
  mock-payments:
    image: vinipx/wixy:latest
    environment: [ "WIXY_UI_ENABLED=false" ]
    # No need to map ports to host if only the Hub needs to talk to them

  # Remote Mock 2: Inventory System
  mock-inventory:
    image: vinipx/wixy:latest
    environment: [ "WIXY_UI_ENABLED=false" ]

  # Remote Mock 3: Shipping Provider
  mock-shipping:
    image: vinipx/wixy:latest
    environment: [ "WIXY_UI_ENABLED=false" ]
```

---

## 🔗 Connecting to the Hub

Once your remote engines are running, add them to the Hub using the **Registry Dashboard** or the API:

```bash
curl -X POST http://localhost:8080/wixy/admin/registry/servers 
  -H "Content-Type: application/json" 
  -d '{
    "name": "Payment Service (Mock)",
    "url": "http://mock-payments:8080"
  }'
```

### Pro-Tip: Network Aliases
In Docker environments, use the **Service Name** (e.g., `http://mock-payments:8080`) as the URL. WIXY Hub will resolve the IP address automatically via Docker's internal DNS.
