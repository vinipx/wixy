---
sidebar_position: 2
title: Unit Tests
---

# Unit Tests

Unit tests validate each class in **complete isolation** — dependencies are mocked using Mockito, no Spring context is loaded, and tests execute in milliseconds. Every unit test class is annotated with `@Tag("unit")` and lives under `src/test/java/io/github/vinipx/wixy/unit/`.

## Running Unit Tests

```bash
# Run all 129 unit tests
./gradlew test

# Run a specific test class
./gradlew test --tests "io.github.vinipx.wixy.unit.service.StubServiceTest"

# Run a specific nested test group
./gradlew test --tests "io.github.vinipx.wixy.unit.service.StubServiceTest\$Create"

# Run with verbose output
./gradlew test --info
```

## Test Structure Conventions

Every unit test class follows these conventions:

```java
package io.github.vinipx.wixy.unit.service;   // mirrors main source package

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")                                    // JUnit 5 tag for filtering
@DisplayName("StubService")                     // human-readable name
class StubServiceTest {                         // *Test suffix

    // Mock dependencies
    private final WireMockServer wireMockServer = mock(WireMockServer.class);
    
    // System under test
    private StubService stubService;

    @BeforeEach
    void setUp() {
        stubService = new StubService(wireMockServer);
    }

    @Nested @DisplayName("create()")            // Grouped by method
    class Create {
        @Test
        @DisplayName("should create a valid stub mapping")
        void validStub() {
            // arrange → act → assert
        }
    }
}
```

**Key patterns:**
- **`@Nested` classes** group tests by the method being tested
- **`@DisplayName`** provides human-readable test names
- **AssertJ** for fluent assertions (`assertThat(...)`)
- **Mockito** for dependency isolation (`mock()`, `when()`, `verify()`)

---

## Test Catalogue by Layer

### Configuration Layer

#### `WixyPropertiesTest` — 17 tests

Validates all default values, setter mutations, and nested class independence for the `@ConfigurationProperties` bean.

```java title="src/test/java/io/github/vinipx/wixy/unit/config/WixyPropertiesTest.java"
@Tag("unit")
@DisplayName("WixyProperties")
class WixyPropertiesTest {

    @Nested @DisplayName("Default values")
    class Defaults {

        private final WixyProperties props = new WixyProperties();

        @Test @DisplayName("wiremock.port defaults to 9090")
        void wiremockPortDefault() {
            assertThat(props.getWiremock().getPort()).isEqualTo(9090);
        }

        @Test @DisplayName("proxy.enabled defaults to false")
        void proxyEnabledDefault() {
            assertThat(props.getProxy().isEnabled()).isFalse();
        }

        @Test @DisplayName("security.enabled defaults to false")
        void securityEnabledDefault() {
            assertThat(props.getSecurity().isEnabled()).isFalse();
        }
    }

    @Nested @DisplayName("Setter mutations")
    class Setters {

        @Test @DisplayName("should set wiremock port")
        void setWiremockPort() {
            var props = new WixyProperties();
            props.getWiremock().setPort(8888);
            assertThat(props.getWiremock().getPort()).isEqualTo(8888);
        }
    }

    @Nested @DisplayName("Nested class independence")
    class NestedClassIsolation {

        @Test @DisplayName("Wiremock instances should be independent")
        void wiremockIndependence() {
            var a = new WixyProperties.Wiremock();
            var b = new WixyProperties.Wiremock();
            a.setPort(1111);
            assertThat(b.getPort()).isEqualTo(9090); // unchanged
        }
    }
}
```

**Covered scenarios:** All 12 default values, all 11 setters, 3 nested class isolation checks.

---

#### `WireMockConfigTest` — 8 tests

Tests WireMock server lifecycle management (`@PostConstruct` / `@PreDestroy`), random port allocation, verbose mode, and proxy setup.

```java title="src/test/java/io/github/vinipx/wixy/unit/config/WireMockConfigTest.java (excerpt)"
@Tag("unit")
@DisplayName("WireMockConfig")
class WireMockConfigTest {

    @Nested @DisplayName("Lifecycle management")
    class Lifecycle {

        @Test @DisplayName("should start WireMock on PostConstruct and stop on PreDestroy")
        void startAndStop() {
            var config = new WireMockConfig(defaultProperties());
            config.start();

            WireMockServer server = config.wireMockServer();
            assertThat(server).isNotNull();
            assertThat(server.isRunning()).isTrue();
            assertThat(config.getActualPort()).isPositive();

            config.stop();
            assertThat(server.isRunning()).isFalse();
        }

        @Test @DisplayName("stop should be idempotent when server already stopped")
        void stopIdempotent() {
            var config = new WireMockConfig(defaultProperties());
            config.start();
            config.stop();
            assertThatCode(config::stop).doesNotThrowAnyException();
        }
    }

    @Nested @DisplayName("Proxy configuration")
    class ProxySetup {

        @Test @DisplayName("should set up catch-all proxy when enabled without record")
        void proxyEnabledNoRecord() {
            var props = defaultProperties();
            props.getProxy().setEnabled(true);
            props.getProxy().setTargetUrl("http://httpbin.org");
            var config = new WireMockConfig(props);
            config.start();
            try {
                assertThat(config.wireMockServer().getStubMappings()).hasSize(1);
            } finally {
                config.stop();
            }
        }
    }
}
```

**Covered scenarios:** Start/stop lifecycle, idempotent stop, port-before-start, random port allocation, different random ports, verbose on/off, proxy disabled/enabled/blank target, bean identity.

---

#### `WireMockHealthIndicatorTest` — 4 tests

```java title="src/test/java/io/github/vinipx/wixy/unit/config/WireMockHealthIndicatorTest.java (excerpt)"
@Nested @DisplayName("When WireMock is running")
class Running {

    @Test @DisplayName("should report UP status with port and stub count")
    void upWithDetails() {
        WireMockServer server = mock(WireMockServer.class);
        when(server.isRunning()).thenReturn(true);
        when(server.port()).thenReturn(9090);
        when(server.getStubMappings()).thenReturn(java.util.List.of());

        var indicator = new WireMockHealthIndicator(server);
        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("port", 9090);
        assertThat(health.getDetails()).containsEntry("stubCount", 0);
    }
}
```

**Covered scenarios:** UP with port/stubCount, UP with multiple stubs, DOWN when stopped, DOWN when null.

---

#### `SecurityConfigTest` — 13 tests

Tests the `ApiKeyFilter` including allow-listed paths, valid/invalid/missing keys, and no-key passthrough.

```java title="src/test/java/io/github/vinipx/wixy/unit/config/SecurityConfigTest.java (excerpt)"
@Tag("unit")
@DisplayName("SecurityConfig")
class SecurityConfigTest {

    private SecurityConfig.ApiKeyFilter createFilter(String expectedKey) {
        return new SecurityConfig.ApiKeyFilter(expectedKey);
    }

    @Nested @DisplayName("Allow-listed paths")
    class AllowedPaths {

        @Test @DisplayName("should allow /actuator/health without API key")
        void actuatorHealth() throws Exception {
            var filter = createFilter("test-key");
            var req = mockRequest("/actuator/health", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);         // passes through
            verify(resp, never()).setStatus(anyInt());  // no 401
        }
    }

    @Nested @DisplayName("Invalid or missing API key")
    class InvalidKey {

        @Test @DisplayName("should reject request with wrong API key")
        void wrongKey() throws Exception {
            var filter = createFilter("correct-key");
            var req = mockRequest("/wixy/admin/mappings", "wrong-key");
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(resp).setStatus(401);
            verify(chain, never()).doFilter(any(), any());
        }
    }
}
```

**Covered scenarios:** 5 allow-listed paths (health, info, swagger-ui, api-docs, sub-paths), correct key, missing key, wrong key, empty key, null expected key (passthrough), blank expected key (passthrough), accessor method, bean factory.

---

### Service Layer

#### `StubServiceTest` — 14 tests

Full CRUD coverage with a real embedded WireMock instance (started once in `@BeforeAll`, reset per test).

```java title="src/test/java/io/github/vinipx/wixy/unit/service/StubServiceTest.java (excerpt)"
@Tag("unit")
@DisplayName("StubService")
class StubServiceTest {

    private static WireMockServer wireMockServer;
    private StubService stubService;

    private static final String VALID_STUB_JSON = """
            {
              "request": { "method": "GET", "urlPath": "/api/test" },
              "response": { "status": 200, "jsonBody": { "message": "hello" } }
            }
            """;

    @BeforeAll static void startServer() {
        wireMockServer = new WireMockServer(0);    // random port
        wireMockServer.start();
    }

    @AfterAll static void stopServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) wireMockServer.stop();
    }

    @BeforeEach void setUp() {
        wireMockServer.resetMappings();
        stubService = new StubService(wireMockServer);
    }

    @Nested @DisplayName("create()")
    class Create {

        @Test @DisplayName("should create a valid stub mapping")
        void validStub() {
            StubMapping created = stubService.create(VALID_STUB_JSON);
            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotNull();
            assertThat(stubService.listAll()).hasSize(1);
        }

        @Test @DisplayName("should throw InvalidStubDefinitionException for malformed JSON")
        void invalidJson() {
            assertThatThrownBy(() -> stubService.create("not json"))
                    .isInstanceOf(InvalidStubDefinitionException.class)
                    .hasMessageContaining("Failed to parse stub definition");
        }
    }

    @Nested @DisplayName("getById()")
    class GetById {

        @Test @DisplayName("should throw StubNotFoundException for unknown UUID")
        void unknownId() {
            UUID fakeId = UUID.randomUUID();
            assertThatThrownBy(() -> stubService.getById(fakeId))
                    .isInstanceOf(StubNotFoundException.class)
                    .hasMessageContaining(fakeId.toString());
        }
    }
}
```

**Covered scenarios:** listAll (empty/populated), create (valid/UUID-assigned/invalid JSON/empty/minimal), getById (found/not-found), update (found/not-found/invalid JSON), delete (found/not-found), resetAll (populated/empty), importStubs (single/malformed).

---

#### `ProxyServiceTest` — 8 tests

```java title="Example: ProxyService enable/disable test"
@Nested @DisplayName("enableProxy()")
class EnableProxy {

    @Test @DisplayName("should enable proxy to a target URL")
    void validTarget() {
        proxyService.enableProxy("http://backend:8080");
        assertThat(properties.getProxy().isEnabled()).isTrue();
        assertThat(properties.getProxy().getTargetUrl()).isEqualTo("http://backend:8080");
        assertThat(wireMockServer.getStubMappings()).isNotEmpty();
    }

    @Test @DisplayName("should throw WixyException when target URL is null")
    void nullTarget() {
        assertThatThrownBy(() -> proxyService.enableProxy(null))
                .isInstanceOf(WixyException.class)
                .hasMessageContaining("Target URL must not be blank");
    }
}
```

**Covered scenarios:** getStatus (default/after-enable), enableProxy (valid/null/blank/whitespace/overwrite), disableProxy (normal/idempotent).

---

#### `RecordingServiceTest` — 8 tests

**Covered scenarios:** getStatus (initial/active), startRecording (explicit URL/fallback-null/fallback-blank/no-target/both-blank), stopRecording (after-start/without-start).

---

### Controller Layer

#### `AdminControllerTest` — 11 tests

Tests HTTP-level delegation using Mockito mocks for the service layer:

```java title="src/test/java/io/github/vinipx/wixy/unit/controller/AdminControllerTest.java (excerpt)"
@Tag("unit")
@DisplayName("AdminController")
class AdminControllerTest {

    private final StubService stubService = mock(StubService.class);
    private final AdminController controller = new AdminController(stubService);

    @Nested @DisplayName("GET /wixy/admin/mappings")
    class ListAll {

        @Test @DisplayName("should return 200 with empty list when no stubs")
        void emptyList() {
            when(stubService.listAll()).thenReturn(List.of());
            var response = controller.listAll();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            @SuppressWarnings("unchecked")
            var body = (Map<String, Object>) response.getBody();
            assertThat(body).containsKey("mappings").containsKey("meta");
        }
    }

    @Nested @DisplayName("POST /wixy/admin/mappings")
    class Create {

        @Test @DisplayName("should return 201 with created stub")
        void success() {
            var stub = mock(StubMapping.class);
            when(stub.toString()).thenReturn("{\"id\":\"abc\"}");
            when(stubService.create("json-input")).thenReturn(stub);
            var response = controller.create("json-input");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test @DisplayName("should propagate InvalidStubDefinitionException")
        void invalidInput() {
            when(stubService.create("bad"))
                    .thenThrow(new InvalidStubDefinitionException("parse error"));
            assertThatThrownBy(() -> controller.create("bad"))
                    .isInstanceOf(InvalidStubDefinitionException.class);
        }
    }
}
```

**Covered scenarios:** listAll (empty/populated/delegate), create (success/invalid), getById (found/not-found), update (success/not-found), delete (success/not-found), resetAll, importStubs.

---

#### `ProxyControllerTest` — 4 tests

**Covered scenarios:** getStatus, enable (success/missing-target), disable.

#### `RecordingControllerTest` — 6 tests

**Covered scenarios:** start (with-URL/null-body/missing-URL/no-target-available), stop, getStatus.

---

### Exception Layer

#### `ExceptionTest` — 16 tests

Covers all exception classes and the `GlobalExceptionHandler` mappings:

```java title="src/test/java/io/github/vinipx/wixy/unit/exception/ExceptionTest.java (excerpt)"
@Nested @DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test @DisplayName("should handle StubNotFoundException as 404")
    void stubNotFound() {
        var response = handler.handleStubNotFound(
                new StubNotFoundException("missing-id"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("status", 404)
                .containsEntry("error", "Not Found");
        assertThat((String) response.getBody().get("message"))
                .contains("missing-id");
    }
}
```

**Covered scenarios:**
- `WixyException`: message, message+cause, RuntimeException inheritance
- `StubNotFoundException`: message contains ID, extends WixyException
- `InvalidStubDefinitionException`: message, message+cause, extends WixyException
- `GlobalExceptionHandler`: StubNotFound→404, InvalidStub→400, WixyException→500, generic→500, timestamp presence, consistent structure

---

### Application Layer

#### `WixyApplicationTest` — 3 tests

**Covered scenarios:** main method exists, `@SpringBootApplication` annotation present, `@EnableConfigurationProperties` annotation present.

---

## Complete Test Summary

| Package | Class | Tests | What's Tested |
|---------|-------|-------|---------------|
| `unit.application` | `WixyApplicationTest` | 3 | Main class, annotations |
| `unit.config` | `WixyPropertiesTest` | 17 | Defaults, setters, nested isolation |
| `unit.config` | `WireMockConfigTest` | 8 | Lifecycle, ports, proxy setup |
| `unit.config` | `WireMockHealthIndicatorTest` | 4 | UP/DOWN states, details |
| `unit.config` | `SecurityConfigTest` | 13 | Allow-list, key validation, filter |
| `unit.controller` | `AdminControllerTest` | 11 | CRUD delegation, HTTP status codes |
| `unit.controller` | `ProxyControllerTest` | 4 | Status, enable, disable |
| `unit.controller` | `RecordingControllerTest` | 6 | Start, stop, status |
| `unit.service` | `StubServiceTest` | 14 | Full CRUD with embedded WireMock |
| `unit.service` | `ProxyServiceTest` | 8 | Enable/disable/status |
| `unit.service` | `RecordingServiceTest` | 8 | Start/stop/status lifecycle |
| `unit.exception` | `ExceptionTest` | 16 | All exceptions + handler |
| | **Total** | **129** | |
