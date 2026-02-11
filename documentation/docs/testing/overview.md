---
sidebar_position: 1
title: Testing Overview
---

# Testing Strategy

WIXY employs a rigorous, two-tier testing strategy that ensures **correctness at every layer** while maintaining **fast feedback cycles**. All tests live under a single source root (`src/test/java/`) with clear separation between **unit** and **integration** tests.

## Test Pyramid

```
              ╱ ╲
             ╱ IT ╲                57 integration tests
            ╱──────╲               Full HTTP against running app
           ╱  Unit   ╲            129 unit tests
          ╱────────────╲           Isolated, mocked dependencies
```

| Tier | Tests | Speed | Scope | Tag |
|------|-------|-------|-------|-----|
| **Unit** | 129 | ~3 seconds | Single class, mocked dependencies | `@Tag("unit")` |
| **Integration** | 57 | ~15 seconds | Full Spring Boot context, real HTTP | `@Tag("integration")` |
| **Total** | **186** | ~18 seconds | — | — |

## Directory Layout

```
src/test/java/io/github/vinipx/wixy/
├── unit/                                      ← UNIT TESTS (@Tag("unit"))
│   ├── application/
│   │   └── WixyApplicationTest.java               3 tests
│   ├── config/
│   │   ├── WixyPropertiesTest.java                17 tests
│   │   ├── WireMockConfigTest.java                 8 tests
│   │   ├── WireMockHealthIndicatorTest.java        4 tests
│   │   └── SecurityConfigTest.java                13 tests
│   ├── controller/
│   │   ├── AdminControllerTest.java               11 tests
│   │   ├── ProxyControllerTest.java                4 tests
│   │   └── RecordingControllerTest.java            6 tests
│   ├── service/
│   │   ├── StubServiceTest.java                   14 tests
│   │   ├── ProxyServiceTest.java                   8 tests
│   │   └── RecordingServiceTest.java               8 tests
│   └── exception/
│       └── ExceptionTest.java                     16 tests
│
└── integration/                               ← INTEGRATION TESTS (@Tag("integration"))
    ├── BaseIntegrationTest.java                   shared superclass
    ├── config/
    │   └── TestEnvironment.java                   local / remote URL resolution
    ├── context/
    │   └── ApplicationContextIT.java               4 tests
    ├── health/
    │   └── HealthEndpointIT.java                   4 tests
    ├── stub/
    │   └── StubManagementIT.java                  13 tests
    ├── proxy/
    │   └── ProxyManagementIT.java                  7 tests
    ├── recording/
    │   └── RecordingIT.java                        6 tests
    ├── security/
    │   └── SecurityIT.java                        10 tests
    ├── swagger/
    │   └── SwaggerIT.java                          6 tests
    └── wiremock/
        └── WireMockResolutionIT.java               7 tests
```

## Design Principles

### Single Source Root

Both unit and integration tests reside under **`src/test/java/`** — a single Gradle source set. The separation is achieved through:

1. **Directory structure** — `unit/` vs `integration/` sub-packages
2. **JUnit 5 tags** — `@Tag("unit")` vs `@Tag("integration")` on every test class
3. **Gradle tasks** — `test` (unit only) vs `integrationTest` (integration only)
4. **Naming convention** — `*Test.java` for unit, `*IT.java` for integration

### Mirror-the-Source Principle

Unit test packages **mirror the main source structure** exactly:

| Main Source | Unit Test |
|-------------|-----------|
| `io.github.vinipx.wixy.config.SecurityConfig` | `io.github.vinipx.wixy.unit.config.SecurityConfigTest` |
| `io.github.vinipx.wixy.service.StubService` | `io.github.vinipx.wixy.unit.service.StubServiceTest` |
| `io.github.vinipx.wixy.controller.AdminController` | `io.github.vinipx.wixy.unit.controller.AdminControllerTest` |
| `io.github.vinipx.wixy.exception.*` | `io.github.vinipx.wixy.unit.exception.ExceptionTest` |

Integration test packages **mirror the domain areas** they exercise:

| Domain | Integration Test |
|--------|-----------------|
| Application bootstrap | `integration.context.ApplicationContextIT` |
| Stub CRUD lifecycle | `integration.stub.StubManagementIT` |
| Proxy management | `integration.proxy.ProxyManagementIT` |
| Recording lifecycle | `integration.recording.RecordingIT` |
| API-key security | `integration.security.SecurityIT` |

## Coverage

JaCoCo enforces a **minimum 80% instruction coverage** threshold. The current suite achieves:

| Metric | Coverage |
|--------|----------|
| **Instruction** | 96.5% |
| **Branch** | 92.0% |
| **Line** | 96.1% |
| **Method** | 97.8% |

The build **fails** if coverage drops below the threshold:

```kotlin title="build.gradle.kts"
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

## Quick Commands

| Command | Description |
|---------|-------------|
| `./gradlew test` | Run unit tests only (129 tests) |
| `./gradlew integrationTest` | Run integration tests only (57 tests) |
| `./gradlew check` | Run both + JaCoCo coverage verification |
| `./gradlew jacocoTestReport` | Generate HTML coverage report |
| `./gradlew integrationTest -Dwixy.test.base-url=https://...` | Run against a remote instance |

:::tip
For detailed examples and code walkthroughs, see the dedicated [Unit Tests](/docs/testing/unit-tests) and [Integration Tests](/docs/testing/integration-tests) guides.
:::
