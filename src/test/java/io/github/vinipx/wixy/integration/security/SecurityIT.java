package io.github.vinipx.wixy.integration.security;

import io.github.vinipx.wixy.WixyApplication;
import io.github.vinipx.wixy.integration.config.TestEnvironment;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@DisplayName("API Key Security")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = WixyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest-secured")
class SecurityIT {

    @LocalServerPort private int port;
    private String baseUrl;

    @BeforeEach void setUpRestAssured() {
        baseUrl = TestEnvironment.resolveBaseUrl(port);
        RestAssured.baseURI = baseUrl;
        try { var uri = java.net.URI.create(baseUrl); RestAssured.port = uri.getPort() > 0 ? uri.getPort() : port; } catch (Exception e) { RestAssured.port = port; }
        RestAssured.basePath = "";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test @Order(1) @DisplayName("GET /actuator/health should be accessible without API key") void healthWithoutApiKey() {
        given().get("/actuator/health").then().statusCode(200);
    }
    @Test @Order(2) @DisplayName("GET /v3/api-docs should be accessible without API key") void apiDocsWithoutApiKey() {
        given().get("/v3/api-docs").then().statusCode(200);
    }
    @Test @Order(3) @DisplayName("GET /wixy/admin/mappings without API key should return 401") void mappingsWithoutApiKey() {
        given().get("/wixy/admin/mappings").then().statusCode(401).body("error", equalTo("Unauthorized"));
    }
    @Test @Order(4) @DisplayName("GET /wixy/admin/mappings with wrong API key should return 401") void mappingsWithWrongApiKey() {
        given().header("X-Wixy-Api-Key", "wrong-key-12345").get("/wixy/admin/mappings").then().statusCode(401);
    }
    @Test @Order(5) @DisplayName("GET /wixy/admin/mappings with correct API key should return 200") void mappingsWithCorrectApiKey() {
        given().header("X-Wixy-Api-Key", TestEnvironment.getApiKey()).get("/wixy/admin/mappings").then().statusCode(200);
    }
    @Test @Order(6) @DisplayName("POST /wixy/admin/mappings with correct API key should return 201") void createWithCorrectApiKey() {
        given().header("X-Wixy-Api-Key", TestEnvironment.getApiKey()).contentType(ContentType.JSON)
                .body("""
                    { "request": { "method": "GET", "urlPath": "/api/secure-test" }, "response": { "status": 200, "jsonBody": { "secure": true } } }
                    """)
                .post("/wixy/admin/mappings").then().statusCode(201);
    }
    @Test @Order(7) @DisplayName("POST /wixy/admin/proxy/enable without API key should return 401") void proxyEnableWithoutApiKey() {
        given().contentType(ContentType.JSON).body("{\"targetUrl\": \"http://example.com\"}").post("/wixy/admin/proxy/enable").then().statusCode(401);
    }
    @Test @Order(8) @DisplayName("GET /wixy/admin/recordings/status without API key should return 401") void recordingStatusWithoutApiKey() {
        given().get("/wixy/admin/recordings/status").then().statusCode(401);
    }
    @Test @Order(9) @DisplayName("GET /wixy/admin/recordings/status with correct API key should return 200") void recordingStatusWithCorrectApiKey() {
        given().header("X-Wixy-Api-Key", TestEnvironment.getApiKey()).get("/wixy/admin/recordings/status").then().statusCode(200);
    }
    @Test @Order(10) @DisplayName("Full secured workflow: create → read → delete with API key") void securedCrudLifecycle() {
        String apiKey = TestEnvironment.getApiKey();
        String body = given().header("X-Wixy-Api-Key", apiKey).contentType(ContentType.JSON)
                .body("""
                    { "request": { "method": "GET", "urlPath": "/api/lifecycle-secured" }, "response": { "status": 200, "jsonBody": { "lifecycle": true } } }
                    """)
                .post("/wixy/admin/mappings").then().statusCode(201).extract().body().asString();
        String id = io.restassured.path.json.JsonPath.from(body).getString("id");
        given().header("X-Wixy-Api-Key", apiKey).get("/wixy/admin/mappings/{id}", id).then().statusCode(200);
        given().header("X-Wixy-Api-Key", apiKey).delete("/wixy/admin/mappings/{id}", id).then().statusCode(204);
        given().header("X-Wixy-Api-Key", apiKey).get("/wixy/admin/mappings/{id}", id).then().statusCode(404);
    }
}
