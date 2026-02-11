---
sidebar_position: 2
title: Developers
---

# Guide for Developers

This guide covers contributing to WIXY, extending its functionality, and understanding the codebase and testing strategy.

## Development Setup

### Prerequisites

| Tool | Version | Installation |
|------|---------|-------------|
| Java | 21+ | `brew install openjdk@21` |
| Gradle | 9.x | Bundled via `gradlew` wrapper |
| Docker | 24+ | `brew install docker` |
| IDE | IntelliJ IDEA (recommended) | — |

### Clone and Build

```bash
git clone https://github.com/vinipx/wixy.git
cd wixy
./gradlew build
```

### Run Locally

```bash
# With Gradle (fastest)
./gradlew bootRun --args='--spring.profiles.active=local'

# Or build and run the fat JAR
./gradlew bootJar -x test
java -jar build/libs/wixy-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

See the [Running Locally](/docs/testing/running-locally) guide for all available methods including Docker and startup scripts.

## Project Structure

```
wixy/
├── src/main/java/io/github/vinipx/wixy/
│   ├── WixyApplication.java          # Entry point
│   ├── config/                        # Configuration beans
│   │   ├── WireMockConfig.java        # WireMock lifecycle (@PostConstruct / @PreDestroy)
│   │   ├── WixyProperties.java        # @ConfigurationProperties (wixy.*)
│   │   ├── SecurityConfig.java        # Optional API-key filter
│   │   └── WireMockHealthIndicator.java  # Custom health indicator
│   ├── controller/                    # REST controllers
│   │   ├── AdminController.java       # Stub CRUD (/wixy/admin/mappings)
│   │   ├── ProxyController.java       # Proxy management (/wixy/admin/proxy)
│   │   └── RecordingController.java   # Recording lifecycle (/wixy/admin/recordings)
│   ├── service/                       # Business logic
│   │   ├── StubService.java           # Stub CRUD operations
│   │   ├── ProxyService.java          # Proxy enable/disable
│   │   └── RecordingService.java      # Recording start/stop
│   └── exception/                     # Exception handling
│       ├── WixyException.java         # Base exception
│       ├── StubNotFoundException.java # 404 for missing stubs
│       ├── InvalidStubDefinitionException.java  # 400 for bad JSON
│       └── GlobalExceptionHandler.java # @ControllerAdvice
├── src/main/resources/
│   ├── application.yml                # Default configuration
│   ├── application-local.yml          # Local dev overrides
│   ├── application-docker.yml         # Docker overrides
│   ├── application-cloud.yml          # Cloud overrides (security ON)
│   └── wiremock/                      # Pre-packaged stubs
│       ├── mappings/sample-stub.json
│       └── __files/sample-response.json
├── src/test/java/io/github/vinipx/wixy/
│   ├── unit/                          # Unit tests (@Tag("unit"))
│   │   ├── application/               # WixyApplicationTest
│   │   ├── config/                    # Properties, WireMock, Health, Security tests
│   │   ├── controller/                # Admin, Proxy, Recording controller tests
│   │   ├── service/                   # Stub, Proxy, Recording service tests
│   │   └── exception/                 # Exception + handler tests
│   └── integration/                   # Integration tests (@Tag("integration"))
│       ├── BaseIntegrationTest.java   # Shared superclass (REST Assured setup)
│       ├── config/TestEnvironment.java # Local/remote URL resolution
│       ├── context/                   # ApplicationContextIT
│       ├── health/                    # HealthEndpointIT
│       ├── stub/                      # StubManagementIT
│       ├── proxy/                     # ProxyManagementIT
│       ├── recording/                 # RecordingIT
│       ├── security/                  # SecurityIT
│       ├── swagger/                   # SwaggerIT
│       └── wiremock/                  # WireMockResolutionIT
├── src/test/resources/
│   ├── application-test.yml           # Unit test config
│   ├── application-integrationtest.yml # Integration test config
│   └── application-integrationtest-secured.yml  # Secured IT config
├── documentation/                     # Docusaurus documentation
├── build.gradle.kts                   # Build configuration
├── Dockerfile                         # Multi-stage Docker build
├── docker-compose.yml                 # Docker Compose
└── docs.sh                            # Documentation dev server
```

## Build Commands

```bash
# Full build with all tests and coverage verification
./gradlew check

# Unit tests only (129 tests, ~3 seconds)
./gradlew test

# Integration tests only (57 tests, ~15 seconds)
./gradlew integrationTest

# Integration tests against a remote instance
./gradlew integrationTest \
    -Dwixy.test.base-url=https://wixy.example.com

# Integration tests against a secured remote instance
./gradlew integrationTest \
    -Dwixy.test.base-url=https://wixy.example.com \
    -Dwixy.test.api-key=your-api-key

# Generate coverage report
./gradlew jacocoTestReport
# View: build/reports/jacoco/test/html/index.html

# Build Docker image
docker build -t wixy:latest .

# Build bootJar only (fast, skip all tests)
./gradlew bootJar -x test
```

## Testing Strategy

WIXY uses a two-tier test pyramid with **186 tests** achieving **96.5% code coverage**.

### Directory Organisation

Both unit and integration tests live under a **single source root** (`src/test/java/`) with clear separation:

```
src/test/java/io/github/vinipx/wixy/
├── unit/                    ← 129 tests, @Tag("unit"), *Test.java naming
│   ├── config/              ← Mirrors src/main/.../config/
│   ├── controller/          ← Mirrors src/main/.../controller/
│   ├── service/             ← Mirrors src/main/.../service/
│   └── exception/           ← Mirrors src/main/.../exception/
└── integration/             ← 57 tests, @Tag("integration"), *IT.java naming
    ├── context/             ← Application bootstrap
    ├── stub/                ← Stub CRUD lifecycle
    ├── proxy/               ← Proxy management
    ├── recording/           ← Recording lifecycle
    ├── security/            ← API-key security
    ├── swagger/             ← OpenAPI / Swagger
    └── wiremock/            ← End-to-end resolution
```

### Separation Mechanism

| Mechanism | Unit | Integration |
|-----------|------|-------------|
| **Directory** | `unit/` | `integration/` |
| **JUnit 5 Tag** | `@Tag("unit")` | `@Tag("integration")` |
| **File naming** | `*Test.java` | `*IT.java` |
| **Gradle task** | `./gradlew test` | `./gradlew integrationTest` |
| **Spring context** | None (mocked) | Full `@SpringBootTest` |
| **Speed** | ~3 seconds | ~15 seconds |

### Unit Tests

Unit tests validate each class in **complete isolation** using Mockito mocks:

```java
@Tag("unit")
@DisplayName("StubService")
class StubServiceTest {
    private final WireMockServer server = mock(WireMockServer.class);
    private final StubService service = new StubService(server);

    @Test void shouldCreateStub() {
        // arrange → act → assert (no Spring context, no network)
    }
}
```

### Integration Tests

Integration tests boot the **full application** on a random port and issue **real HTTP requests**:

```java
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
class StubManagementIT extends BaseIntegrationTest {

    @Test void createStub() {
        given().contentType(ContentType.JSON)
                .body(TEST_STUB_JSON)
                .post("/wixy/admin/mappings")
                .then().statusCode(201);
    }
}
```

Integration tests can target **any remote instance** — not just the local embedded server:

```bash
./gradlew integrationTest -Dwixy.test.base-url=https://staging.example.com
```

### Coverage

JaCoCo enforces a **minimum 80% instruction coverage** threshold. Current: **96.5%**.

```kotlin title="build.gradle.kts"
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule { limit { minimum = "0.80".toBigDecimal() } }
    }
}
```

:::info
For comprehensive testing documentation with full code examples, see:
- **[Testing Overview](/docs/testing/overview)** — Strategy, directory layout, coverage metrics
- **[Unit Tests](/docs/testing/unit-tests)** — All 129 tests with annotated code examples
- **[Integration Tests](/docs/testing/integration-tests)** — All 57 tests with remote configuration
- **[Running Locally](/docs/testing/running-locally)** — Local development and testing workflows
:::

## Adding a New Feature

### 1. Add the Service

```java
package io.github.vinipx.wixy.service;

@Service
public class MyNewService {
    private final WireMockServer wireMockServer;

    public MyNewService(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }
    // ... implement business logic
}
```

### 2. Add the Controller

```java
package io.github.vinipx.wixy.controller;

@RestController
@RequestMapping("/wixy/admin/my-feature")
@Tag(name = "My Feature", description = "Description")
public class MyFeatureController {
    private final MyNewService service;

    public MyFeatureController(MyNewService service) {
        this.service = service;
    }
    // ... expose REST endpoints
}
```

### 3. Add Tests

Every feature requires both unit and integration tests:

**Unit test** — in `src/test/java/io/github/vinipx/wixy/unit/service/MyNewServiceTest.java`:

```java
@Tag("unit")
@DisplayName("MyNewService")
class MyNewServiceTest {
    private final WireMockServer server = mock(WireMockServer.class);
    private final MyNewService service = new MyNewService(server);

    @Test void shouldDoSomething() {
        // test with mocked WireMock
    }
}
```

**Integration test** — in `src/test/java/io/github/vinipx/wixy/integration/myfeature/MyFeatureIT.java`:

```java
@DisplayName("My Feature API")
class MyFeatureIT extends BaseIntegrationTest {

    @Test void shouldReturnData() {
        given().get("/wixy/admin/my-feature")
                .then().statusCode(200);
    }
}
```

### 4. Update Documentation

Add documentation in `documentation/docs/` and update `sidebars.js`.

## Code Style

| Aspect | Standard |
|--------|----------|
| **Language** | Java 21+ features (records, pattern matching, sealed classes, text blocks) |
| **Dependencies** | Constructor injection, no field injection |
| **Logging** | SLF4J via `LoggerFactory` |
| **API Docs** | SpringDoc `@Operation` and `@Tag` annotations |
| **Validation** | Jakarta Bean Validation on config properties |
| **Exceptions** | Domain-specific exceptions extending `WixyException` |
| **Tests** | JUnit 5 + Mockito (unit), RestAssured (integration) |
| **Assertions** | AssertJ (`assertThat`) for unit, Hamcrest for REST Assured |

## Dependencies

```kotlin title="build.gradle.kts"
dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // WireMock
    implementation("org.wiremock:wiremock-standalone:3.13.0")

    // OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured:5.5.1")
    testImplementation("org.awaitility:awaitility:4.3.0")
}
```
