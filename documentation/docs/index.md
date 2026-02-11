---
slug: /
sidebar_position: 1
title: Introduction
---

# WIXY — Enterprise WireMock Proxy Server

**WIXY** is a lightweight, configurable **test proxy service** built as a Spring Boot application that embeds a [WireMock](https://wiremock.org/) server. It provides enterprise-grade HTTP service virtualisation with stub management, traffic recording, and proxy forwarding — all deployable with a single command.

## Why WIXY?

Modern microservice architectures create complex dependency chains that make integration testing fragile and slow. WIXY solves this by providing:

- **Instant Service Virtualisation** — Stub any HTTP dependency in seconds via REST API
- **Traffic Recording** — Capture real traffic and replay it in isolation
- **Transparent Proxying** — Forward unmatched requests to upstream services
- **Zero Infrastructure** — Runs as a single JAR with embedded WireMock
- **Cloud-Native** — Docker, Kubernetes, and CI/CD ready out of the box

## Key Features

| Feature | Description |
|---------|-------------|
| **Stub CRUD API** | Create, read, update, and delete HTTP stubs at runtime |
| **Pre-packaged Stubs** | Load stubs from JSON files on startup |
| **Proxy Mode** | Forward unmatched requests to a configurable upstream |
| **Record & Playback** | Capture traffic and auto-generate stub mappings |
| **API-Key Security** | Optional authentication for shared environments |
| **Health Monitoring** | Spring Actuator with custom WireMock health indicator |
| **OpenAPI / Swagger** | Auto-generated interactive API documentation |
| **Profile-Based Config** | `local`, `docker`, `cloud` profiles with env-var overrides |

## Architecture at a Glance

```
┌──────────────────────────────────────────────────────────────┐
│                        WIXY (Spring Boot)                    │
│                                                              │
│  ┌──────────────┐   ┌────────────────┐   ┌───────────────┐  │
│  │  Admin API   │   │  Proxy Router  │   │  Stub Store   │  │
│  │ /wixy/admin  │──▶│  (delegates to │──▶│  (WireMock    │  │
│  │  Controller  │   │   WireMock)    │   │   mappings)   │  │
│  └──────────────┘   └───────┬────────┘   └───────────────┘  │
│                             │                                │
│                    ┌────────▼────────┐                       │
│                    │ Embedded        │                       │
│                    │ WireMockServer  │                       │
│                    │ (port 9090)     │                       │
│                    └────────┬────────┘                       │
│                             │ unmatched                      │
│  ┌──────────────┐  ┌───────▼────────┐                       │
│  │  Spring Boot  │  │ Proxy/Record  │                       │
│  │  Actuator     │  │ to upstream   │                       │
│  │  (port 8080)  │  └───────────────┘                       │
│  └──────────────┘                                            │
└──────────────────────────────────────────────────────────────┘
```

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 3.4.x |
| Mock Engine | WireMock | 3.13.x (standalone) |
| Build | Gradle | 9.x (Kotlin DSL) |
| API Docs | SpringDoc OpenAPI | 2.x |
| Testing | JUnit 5 + RestAssured | 186 tests (96.5% coverage) |
| Coverage | JaCoCo | ≥ 80% enforced |
| Containers | Docker | Multi-stage |

## Quick Navigation

- **[Getting Started →](/docs/getting-started/quickstart)** — Install and run WIXY in under a minute
- **[Architecture →](/docs/architecture/overview)** — Understand the design decisions and component layout
- **[API Reference →](/docs/api/rest-endpoints)** — Complete REST API documentation
- **[Testing →](/docs/testing/overview)** — Unit tests, integration tests, and coverage strategy
- **[Running Locally →](/docs/testing/running-locally)** — Run and test WIXY on your machine
- **[Use Cases →](/docs/examples/use-cases)** — Real-world examples and patterns
- **[Deployment →](/docs/deployment/docker)** — Docker, Kubernetes, and CI/CD guides

## Testing

WIXY includes comprehensive testing capabilities:

- **Unit Tests**: Validate individual components in isolation.
- **Integration Tests**: Ensure the system works as a whole.
- **Running Locally**: Guides for setting up and testing on your machine.

See the [Testing Documentation](testing/overview) for details.
