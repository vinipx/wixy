---
sidebar_position: 1
title: Contributing Guidelines
---

# Contributing to WIXY

We welcome contributions! This guide covers the standards and workflow for contributing to WIXY.

## Getting Started

```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/YOUR-USERNAME/wixy.git
cd wixy

# Add upstream remote
git remote add upstream https://github.com/vinipx/wixy.git

# Create a feature branch
git checkout -b feature/my-awesome-feature
```

## Development Workflow

### 1. Make Changes

Follow the existing code patterns:

- **Controllers** in `io.github.vinipx.wixy.controller`
- **Services** in `io.github.vinipx.wixy.service`
- **Configuration** in `io.github.vinipx.wixy.config`
- **Exceptions** in `io.github.vinipx.wixy.exception`

### 2. Write Tests

Every change must include tests in both tiers:

**Unit tests** — in `src/test/java/io/github/vinipx/wixy/unit/<layer>/`:

```java
@Tag("unit")
@DisplayName("MyService")
class MyServiceTest {
    private final SomeDependency dep = mock(SomeDependency.class);
    private final MyService service = new MyService(dep);

    @Test @DisplayName("should handle valid input")
    void validInput() {
        // arrange → act → assert
    }
}
```

**Integration tests** — in `src/test/java/io/github/vinipx/wixy/integration/<domain>/`:

```java
@DisplayName("My Feature API")
class MyFeatureIT extends BaseIntegrationTest {

    @Test @DisplayName("should return 200")
    void success() {
        given().get("/wixy/admin/my-feature")
                .then().statusCode(200);
    }
}
```

See the [Testing documentation](/docs/testing/overview) for the complete test strategy, code examples, and conventions.

### 3. Build and Verify

```bash
# Full build with all tests and coverage verification
./gradlew check

# This runs:
# - 129 unit tests (@Tag("unit"))
# - 57 integration tests (@Tag("integration"))
# - JaCoCo coverage verification (≥ 80%)
```

### 4. Submit a Pull Request

```bash
git add .
git commit -m "feat: add my awesome feature"
git push origin feature/my-awesome-feature
```

Open a PR against `main` with:
- Clear description of changes
- Link to related issue (if any)
- Test coverage summary

## Code Standards

| Aspect | Standard |
|--------|----------|
| **Java version** | 21+ (use modern features: text blocks, records, pattern matching) |
| **Dependency injection** | Constructor injection only |
| **Logging** | SLF4J via `LoggerFactory.getLogger(...)` |
| **API documentation** | `@Operation` and `@Tag` annotations |
| **Configuration** | `@ConfigurationProperties` with Jakarta Bean Validation |
| **Exceptions** | Extend `WixyException` |
| **Unit tests** | JUnit 5 + Mockito + AssertJ, `@Tag("unit")`, `*Test.java` |
| **Integration tests** | JUnit 5 + RestAssured + Hamcrest, `@Tag("integration")`, `*IT.java` |

## Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add bulk delete endpoint
fix: handle null target URL in proxy service
docs: update API reference with new endpoints
test: add integration tests for recording
refactor: extract common validation logic
```

## Project Structure

```
wixy/
├── src/main/java/io/github/vinipx/wixy/     # Application source
├── src/main/resources/                       # Configuration files
├── src/test/java/io/github/vinipx/wixy/
│   ├── unit/                                 # Unit tests (@Tag("unit"))
│   │   ├── config/                           # Config layer tests
│   │   ├── controller/                       # Controller layer tests
│   │   ├── service/                          # Service layer tests
│   │   ├── exception/                        # Exception tests
│   │   └── application/                      # Application class tests
│   └── integration/                          # Integration tests (@Tag("integration"))
│       ├── BaseIntegrationTest.java          # Shared REST Assured setup
│       ├── config/TestEnvironment.java       # Local/remote resolution
│       ├── context/                          # ApplicationContextIT
│       ├── health/                           # HealthEndpointIT
│       ├── stub/                             # StubManagementIT
│       ├── proxy/                            # ProxyManagementIT
│       ├── recording/                        # RecordingIT
│       ├── security/                         # SecurityIT
│       ├── swagger/                          # SwaggerIT
│       └── wiremock/                         # WireMockResolutionIT
├── documentation/                            # Docusaurus documentation
├── build.gradle.kts                          # Build configuration
├── Dockerfile                                # Docker image
├── docker-compose.yml                        # Docker Compose
└── docs.sh                                   # Documentation dev server
```

## Documentation Changes

Documentation lives in `documentation/docs/`. To preview changes:

```bash
# Using the docs.sh script
./docs.sh serve
# Opens at http://localhost:3000/wixy/

# Or manually
cd documentation
npm install
npm start
```

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](https://github.com/vinipx/wixy/blob/main/LICENSE).
