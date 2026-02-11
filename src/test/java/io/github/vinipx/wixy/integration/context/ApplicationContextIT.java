package io.github.vinipx.wixy.integration.context;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Application Context Bootstrap")
class ApplicationContextIT extends BaseIntegrationTest {

    @Test @Order(1) @DisplayName("Application context should load successfully") void contextLoads() {}

    @Test @Order(2) @DisplayName("All REST endpoints should be registered") void allEndpointsRegistered() {
        given().get("/wixy/admin/mappings").then().statusCode(200);
        given().get("/wixy/admin/proxy").then().statusCode(200);
        given().get("/wixy/admin/recordings/status").then().statusCode(200);
        given().get("/actuator/health").then().statusCode(200);
    }

    @Test @Order(3) @DisplayName("WireMock server should be running and healthy") void wireMockHealthy() {
        given().get("/actuator/health").then().statusCode(200)
                .body("status", equalTo("UP"))
                .body("components.wiremock.status", equalTo("UP"))
                .body("components.wiremock.details.port", greaterThan(0));
    }

    @Test @Order(4) @DisplayName("OpenAPI spec should be generated") void openApiGenerated() {
        given().get("/v3/api-docs").then().statusCode(200)
                .body("openapi", notNullValue()).body("info.title", notNullValue());
    }
}
