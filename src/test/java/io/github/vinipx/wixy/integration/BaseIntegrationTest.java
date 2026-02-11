package io.github.vinipx.wixy.integration;

import io.github.vinipx.wixy.WixyApplication;
import io.github.vinipx.wixy.integration.config.TestEnvironment;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

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

    protected void resetAllStubs() {
        RestAssured.given()
                .when()
                .post("/wixy/admin/mappings/reset")
                .then()
                .statusCode(200);
    }

    private int extractPort(String url) {
        try {
            var uri = java.net.URI.create(url);
            int p = uri.getPort();
            if (p > 0) return p;
            return url.startsWith("https") ? 443 : 80;
        } catch (Exception e) {
            return port;
        }
    }

    protected static final String TEST_STUB_JSON = """
            {
              "request": { "method": "GET", "urlPath": "/api/integration-test" },
              "response": { "status": 200, "headers": { "Content-Type": "application/json" }, "jsonBody": { "message": "integration-test-ok" } }
            }
            """;

    protected static final String UPDATED_STUB_JSON = """
            {
              "request": { "method": "GET", "urlPath": "/api/integration-updated" },
              "response": { "status": 200, "headers": { "Content-Type": "application/json" }, "jsonBody": { "message": "updated-ok" } }
            }
            """;
}
