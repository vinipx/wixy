---
sidebar_position: 99
title: Changelog
---

# Changelog

All notable changes to WIXY are documented here. This project follows [Semantic Versioning](https://semver.org/).

---

## [0.1.0-SNAPSHOT] — 2025-07-14

### 🚀 Initial Release

**Core Features:**
- Embedded WireMock server with Spring Boot lifecycle management
- Full CRUD REST API for stub management (`/wixy/admin/mappings`)
- Proxy mode with configurable upstream forwarding
- Record & Playback for traffic capture and stub generation
- Optional API-key security via `X-Wixy-Api-Key` header
- Custom WireMock health indicator for Spring Actuator
- OpenAPI / Swagger UI auto-generated documentation

**Configuration:**
- Profile-based configuration (`local`, `docker`, `cloud`)
- 12-factor environment variable support
- Jakarta Bean Validation on all properties

**Deployment:**
- Multi-stage Dockerfile (Eclipse Temurin 21)
- Docker Compose with health checks
- GitHub Actions workflow for documentation deployment to GitHub Pages

**Testing (186 tests, 96.5% coverage):**
- 129 unit tests with JUnit 5 + Mockito + AssertJ
- 57 integration tests with RestAssured (configurable local/remote)
- JaCoCo coverage enforcement (≥ 80%)
- Organised under `src/test/java/` with `unit/` and `integration/` sub-packages
- JUnit 5 `@Tag` annotations for selective execution
- `TestEnvironment` utility for transparent local/remote targeting

**Package:** `io.github.vinipx.wixy`

**Technology Stack:**
- Java 21 (LTS)
- Spring Boot 3.4.5
- WireMock 3.13.0 (standalone)
- Gradle 9.x (Kotlin DSL)
- SpringDoc OpenAPI 2.8.8

---

## Roadmap

| Feature | Priority | Status |
|---------|----------|--------|
| Stub persistence to database | Medium | Planned |
| GitHub Actions CI/CD pipeline | High | Planned |
| Helm chart for Kubernetes | Medium | Planned |
| WebSocket stubbing support | Low | Backlog |
| gRPC stubbing via WireMock extensions | Low | Backlog |
| Request/response logging dashboard | Low | Backlog |
| Rate-limiting simulation | Medium | Backlog |
