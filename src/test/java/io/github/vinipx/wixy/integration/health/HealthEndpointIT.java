package io.github.vinipx.wixy.integration.health;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Health & Actuator Endpoints")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HealthEndpointIT extends BaseIntegrationTest {

    @Test @Order(1) @DisplayName("GET /actuator/health should return 200 with UP status") void healthEndpoint() {
        given().get("/actuator/health").then().statusCode(200).body("status", equalTo("UP"));
    }
    @Test @Order(2) @DisplayName("GET /actuator/health should include WireMock component details") void healthIncludesWireMockDetails() {
        given().get("/actuator/health").then().statusCode(200).body("components.wiremock.status", equalTo("UP")).body("components.wiremock.details.port", notNullValue());
    }
    @Test @Order(3) @DisplayName("GET /actuator/info should return 200") void infoEndpoint() {
        given().get("/actuator/info").then().statusCode(200);
    }
    @Test @Order(4) @DisplayName("GET /actuator/metrics should return 200") void metricsEndpoint() {
        given().get("/actuator/metrics").then().statusCode(200).body("names", notNullValue());
    }
}
