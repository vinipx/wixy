---
sidebar_position: 3
title: Integration Tests
---

# Integration Tests

Integration tests boot the **full Spring Boot application** with an embedded WireMock server and issue **real HTTP requests** using REST Assured. Every integration test class is annotated with `@Tag("integration")`, uses the `*IT.java` naming convention, and lives under `src/test/java/io/github/vinipx/wixy/integration/`.

## Running Integration Tests

### Against the Local Embedded Instance (Default)

```bash
# Run all 57 integration tests (starts the app automatically)
./gradlew integrationTest

# Run a specific test class
./gradlew integrationTest \
    --tests "io.github.vinipx.wixy.integration.stub.StubManagementIT"

# Run with verbose output
./gradlew integrationTest --info
```

### Against a Remote Instance

Integration tests are **configurable** to run against any remote WIXY instance — staging, QA, production, or cloud. Pass the target URL and optional API key as system properties:

```bash
# Run against a remote staging instance
./gradlew integrationTest \
    -Dwixy.test.base-url=https://staging-wixy.example.com

# Run against a secured remote instance
./gradlew integrationTest \
    -Dwixy.test.base-url=https://production-wixy.example.com \
    -Dwixy.test.api-key=your-production-api-key

# Run a specific test class against a remote instance
./gradlew integrationTest \
    --tests "io.github.vinipx.wixy.integration.health.HealthEndpointIT" \
    -Dwixy.test.base-url=https://staging-wixy.example.com
```

### In CI/CD Pipelines

```yaml title=".github/workflows/ci.yml (excerpt)"
- name: Integration tests against staging
  run: |
    ./gradlew integrationTest \
      -Dwixy.test.base-url=${{ secrets.STAGING_WIXY_URL }} \
      -Dwixy.test.api-key=${{ secrets.STAGING_WIXY_API_KEY }}
```

---

## Remote Configuration Infrastructure

### `TestEnvironment` Utility

The `TestEnvironment` class transparently resolves whether tests should target a local embedded instance or a remote host:

```java title="src/test/java/io/github/vinipx/wixy/integration/config/TestEnvironment.java"
package io.github.vinipx.wixy.integration.config;

public final class TestEnvironment {

    public static final String BASE_URL_PROPERTY = "wixy.test.base-url";
    public static final String API_KEY_PROPERTY = "wixy.test.api-key";
    public static final String DEFAULT_TEST_API_KEY = "integration-test-key-99999";

    private TestEnvironment() {}

    /** Returns true if a remote base URL is configured via system property. */
    public static boolean isRemote() {
        String url = System.getProperty(BASE_URL_PROPERTY, "");
        return !url.isBlank();
    }

    /** Returns the remote base URL from system properties. */
    public static String getRemoteBaseUrl() {
        return System.getProperty(BASE_URL_PROPERTY, "").replaceAll("/+$", "");
    }

    /** Returns the API key (from system property or default test key). */
    public static String getApiKey() {
        String key = System.getProperty(API_KEY_PROPERTY, "");
        return (key != null && !key.isBlank()) ? key : DEFAULT_TEST_API_KEY;
    }

    /** Resolves the base URL — remote if configured, local otherwise. */
    public static String resolveBaseUrl(int localPort) {
        if (isRemote()) {
            return getRemoteBaseUrl();
        }
        return "http://localhost:" + localPort;
    }
}
```

| System Property | Purpose | Default |
|----------------|---------|---------|
| `wixy.test.base-url` | Target host URL (e.g., `https://staging.example.com`) | Empty → use local |
| `wixy.test.api-key` | API key for secured instances | `integration-test-key-99999` |

### `BaseIntegrationTest` Superclass

All integration tests extend `BaseIntegrationTest`, which handles REST Assured configuration and provides shared helpers:

```java title="src/test/java/io/github/vinipx/wixy/integration/BaseIntegrationTest.java"
package io.github.vinipx.wixy.integration;

@Tag("integration")
@SpringBootTest(
        classes = WixyApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integrationtest")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    protected String baseUrl;

    @BeforeEach
    void setUpRestAssured() {
        baseUrl = TestEnvironment.resolveBaseUrl(port);
        RestAssured.baseURI = baseUrl;
        RestAssured.port = extractPort(baseUrl);
        RestAssured.basePath = "";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /** Helper: create a stub and assert 201 */
    protected String createStub(String stubJson) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(stubJson)
                .when()
                .post("/wixy/admin/mappings")
                .then()
                .statusCode(201)
                .extract().body().asString();
    }

    /** Helper: reset all stubs */
    protected void resetAllStubs() {
        RestAssured.given()
                .when()
                .post("/wixy/admin/mappings/reset")
                .then()
                .statusCode(200);
    }

    // Shared test fixtures
    protected static final String TEST_STUB_JSON = """
            {
              "request": { "method": "GET", "urlPath": "/api/integration-test" },
              "response": { "status": 200, "headers": { "Content-Type": "application/json" },
                            "jsonBody": { "message": "integration-test-ok" } }
            }
            """;
}
```

**Key design points:**
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` — avoids port conflicts in parallel CI
- `@ActiveProfiles("integrationtest")` — uses dedicated test configuration
- `TestEnvironment.resolveBaseUrl(port)` — seamless local/remote switching
- Shared `createStub()` and `resetAllStubs()` helpers for DRY test code

### Test Profiles

Integration tests use dedicated Spring profiles:

```yaml title="src/test/resources/application-integrationtest.yml"
wixy:
  wiremock:
    port: 0                # Random port for parallel safety
    verbose: false
  proxy:
    enabled: false
    target-url: ""
  security:
    enabled: false
    api-key: ""
```

```yaml title="src/test/resources/application-integrationtest-secured.yml"
wixy:
  wiremock:
    port: 0
    verbose: false
  proxy:
    enabled: false
  security:
    enabled: true
    api-key: "integration-test-key-99999"
```

---

## Test Catalogue by Domain

### Application Context — `ApplicationContextIT` (4 tests)

Validates that the Spring context loads and all endpoints are registered:

```java title="src/test/java/io/github/vinipx/wixy/integration/context/ApplicationContextIT.java"
@DisplayName("Application Context Bootstrap")
class ApplicationContextIT extends BaseIntegrationTest {

    @Test @DisplayName("Application context should load successfully")
    void contextLoads() {}

    @Test @DisplayName("All REST endpoints should be registered")
    void allEndpointsRegistered() {
        given().get("/wixy/admin/mappings").then().statusCode(200);
        given().get("/wixy/admin/proxy").then().statusCode(200);
        given().get("/wixy/admin/recordings/status").then().statusCode(200);
        given().get("/actuator/health").then().statusCode(200);
    }

    @Test @DisplayName("WireMock server should be running and healthy")
    void wireMockHealthy() {
        given().get("/actuator/health").then().statusCode(200)
                .body("status", equalTo("UP"))
                .body("components.wiremock.status", equalTo("UP"))
                .body("components.wiremock.details.port", greaterThan(0));
    }

    @Test @DisplayName("OpenAPI spec should be generated")
    void openApiGenerated() {
        given().get("/v3/api-docs").then().statusCode(200)
                .body("openapi", notNullValue())
                .body("info.title", notNullValue());
    }
}
```

---

### Stub Management — `StubManagementIT` (13 tests)

Full CRUD lifecycle with real HTTP requests. Every test starts with `resetAllStubs()` to ensure test isolation:

```java title="src/test/java/io/github/vinipx/wixy/integration/stub/StubManagementIT.java (excerpt)"
@DisplayName("Stub Management API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StubManagementIT extends BaseIntegrationTest {

    @BeforeEach void resetState() { resetAllStubs(); }

    @Test @DisplayName("POST /wixy/admin/mappings should create a stub and return 201")
    void createStubMapping() {
        given().contentType(ContentType.JSON)
                .body(TEST_STUB_JSON)
                .post("/wixy/admin/mappings")
                .then().statusCode(201)
                .body(containsString("integration-test"));
    }

    @Test @DisplayName("POST /wixy/admin/mappings with invalid JSON should return 400")
    void createInvalidStub() {
        given().contentType(ContentType.JSON)
                .body("not valid json")
                .post("/wixy/admin/mappings")
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test @DisplayName("Full CRUD lifecycle: create → read → update → delete")
    void fullLifecycle() {
        // Create
        String body = createStub(TEST_STUB_JSON);
        String id = JsonPath.from(body).getString("id");

        // Read
        given().get("/wixy/admin/mappings/{id}", id).then().statusCode(200);

        // Update
        given().contentType(ContentType.JSON)
                .body(UPDATED_STUB_JSON)
                .put("/wixy/admin/mappings/{id}", id).then().statusCode(200);

        // Delete
        given().delete("/wixy/admin/mappings/{id}", id).then().statusCode(204);

        // Verify gone
        given().get("/wixy/admin/mappings/{id}", id).then().statusCode(404);
    }
}
```

**Full list:** listAllEmpty, create, createInvalid, listAfterCreate, getById, getByIdNotFound, updateStub, updateNotFound, deleteStub, deleteNotFound, resetAll, importStubs, fullLifecycle.

---

### Health & Actuator — `HealthEndpointIT` (4 tests)

```java
@Test @DisplayName("GET /actuator/health should return 200 with UP status")
void healthEndpoint() {
    given().get("/actuator/health").then()
            .statusCode(200)
            .body("status", equalTo("UP"));
}

@Test @DisplayName("GET /actuator/health should include WireMock component details")
void healthIncludesWireMockDetails() {
    given().get("/actuator/health").then()
            .statusCode(200)
            .body("components.wiremock.status", equalTo("UP"))
            .body("components.wiremock.details.port", notNullValue());
}
```

**Full list:** health UP status, WireMock component details, info endpoint, metrics endpoint.

---

### Proxy Management — `ProxyManagementIT` (7 tests)

```java
@Test @DisplayName("POST /wixy/admin/proxy/enable should enable proxy to target URL")
void enableProxy() {
    given().contentType(ContentType.JSON)
            .body("{\"targetUrl\": \"http://httpbin.org\"}")
            .post("/wixy/admin/proxy/enable")
            .then().statusCode(200)
            .body("status", equalTo("Proxy enabled"))
            .body("targetUrl", equalTo("http://httpbin.org"));

    // Verify status reflects the change
    given().get("/wixy/admin/proxy").then()
            .body("enabled", equalTo(true))
            .body("targetUrl", equalTo("http://httpbin.org"));
}
```

**Full list:** getStatus, enable (valid/blank/missing), disable, disable-idempotent, fullLifecycle.

---

### Recording — `RecordingIT` (6 tests)

**Full list:** getStatus, startRecording, startRecordingNoTarget, stopRecording, stopWithoutStart, fullLifecycle.

---

### API-Key Security — `SecurityIT` (10 tests)

`SecurityIT` uses a **separate Spring profile** (`integrationtest-secured`) that enables API-key authentication:

```java title="src/test/java/io/github/vinipx/wixy/integration/security/SecurityIT.java (excerpt)"
@Tag("integration")
@SpringBootTest(classes = WixyApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest-secured")  // ← security enabled
class SecurityIT {

    @Test @DisplayName("GET /actuator/health should be accessible without API key")
    void healthWithoutApiKey() {
        given().get("/actuator/health").then().statusCode(200);
    }

    @Test @DisplayName("GET /wixy/admin/mappings without API key should return 401")
    void mappingsWithoutApiKey() {
        given().get("/wixy/admin/mappings").then()
                .statusCode(401)
                .body("error", equalTo("Unauthorized"));
    }

    @Test @DisplayName("GET /wixy/admin/mappings with correct API key should return 200")
    void mappingsWithCorrectApiKey() {
        given().header("X-Wixy-Api-Key", TestEnvironment.getApiKey())
                .get("/wixy/admin/mappings")
                .then().statusCode(200);
    }

    @Test @DisplayName("Full secured workflow: create → read → delete with API key")
    void securedCrudLifecycle() {
        String apiKey = TestEnvironment.getApiKey();

        // Create (requires key)
        String body = given()
                .header("X-Wixy-Api-Key", apiKey)
                .contentType(ContentType.JSON)
                .body("""
                    { "request": { "method": "GET", "urlPath": "/api/lifecycle-secured" },
                      "response": { "status": 200, "jsonBody": { "lifecycle": true } } }
                    """)
                .post("/wixy/admin/mappings").then().statusCode(201)
                .extract().body().asString();

        String id = JsonPath.from(body).getString("id");

        // Read (requires key)
        given().header("X-Wixy-Api-Key", apiKey)
                .get("/wixy/admin/mappings/{id}", id).then().statusCode(200);

        // Delete (requires key)
        given().header("X-Wixy-Api-Key", apiKey)
                .delete("/wixy/admin/mappings/{id}", id).then().statusCode(204);

        // Verify gone
        given().header("X-Wixy-Api-Key", apiKey)
                .get("/wixy/admin/mappings/{id}", id).then().statusCode(404);
    }
}
```

**Full list:** healthWithoutApiKey, apiDocsWithoutApiKey, mappingsWithoutApiKey, mappingsWithWrongApiKey, mappingsWithCorrectApiKey, createWithCorrectApiKey, proxyEnableWithoutApiKey, recordingStatusWithoutApiKey, recordingStatusWithCorrectApiKey, securedCrudLifecycle.

---

### Swagger / OpenAPI — `SwaggerIT` (6 tests)

**Full list:** apiDocsEndpoint, containsStubManagementTag, containsProxyManagementTag, containsRecordingTag, containsAdminMappingPaths, swaggerUiRedirect.

---

### WireMock Resolution — `WireMockResolutionIT` (7 tests)

End-to-end verification that dynamically created stubs are properly registered and resolvable:

**Full list:** prePackagedStub, dynamicStubResolution, multipleStubs, differentMethods, resetClearsAll, customHeaders, variousStatusCodes.

---

## Complete Integration Test Summary

| Package | Class | Tests | Domain |
|---------|-------|-------|--------|
| `integration.context` | `ApplicationContextIT` | 4 | Bootstrap, endpoint registration, WireMock health |
| `integration.health` | `HealthEndpointIT` | 4 | Actuator endpoints, WireMock details |
| `integration.stub` | `StubManagementIT` | 13 | Full CRUD lifecycle |
| `integration.proxy` | `ProxyManagementIT` | 7 | Enable, disable, status |
| `integration.recording` | `RecordingIT` | 6 | Start, stop, status |
| `integration.security` | `SecurityIT` | 10 | API-key filter, allow-listed paths |
| `integration.swagger` | `SwaggerIT` | 6 | OpenAPI spec, Swagger UI |
| `integration.wiremock` | `WireMockResolutionIT` | 7 | End-to-end stub resolution |
| | **Total** | **57** | |

---

## Writing New Integration Tests

To add a new integration test:

1. Create a class under `src/test/java/io/github/vinipx/wixy/integration/<domain>/`
2. Extend `BaseIntegrationTest` (or set up REST Assured manually for custom profiles)
3. Annotate with `@Tag("integration")` and use the `*IT.java` naming convention
4. Use `@BeforeEach` to reset state (e.g., `resetAllStubs()`) for test isolation

```java
package io.github.vinipx.wixy.integration.myfeature;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("My New Feature")
class MyFeatureIT extends BaseIntegrationTest {

    @Test @DisplayName("should do something")
    void shouldDoSomething() {
        given()
                .get("/wixy/admin/my-feature")
                .then()
                .statusCode(200)
                .body("key", equalTo("value"));
    }
}
```
