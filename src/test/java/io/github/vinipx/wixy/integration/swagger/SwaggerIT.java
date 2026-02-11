package io.github.vinipx.wixy.integration.swagger;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Swagger / OpenAPI Endpoints")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwaggerIT extends BaseIntegrationTest {

    @Test @Order(1) @DisplayName("GET /v3/api-docs should return OpenAPI spec") void apiDocsEndpoint() {
        given().get("/v3/api-docs").then().statusCode(200).contentType(ContentType.JSON).body("openapi", startsWith("3.")).body("paths", notNullValue());
    }
    @Test @Order(2) @DisplayName("OpenAPI spec should document the Stub Management tag") void containsStubManagementTag() {
        given().get("/v3/api-docs").then().statusCode(200).body("tags.name", hasItem("Stub Management"));
    }
    @Test @Order(3) @DisplayName("OpenAPI spec should document the Proxy Management tag") void containsProxyManagementTag() {
        given().get("/v3/api-docs").then().statusCode(200).body("tags.name", hasItem("Proxy Management"));
    }
    @Test @Order(4) @DisplayName("OpenAPI spec should document the Recording tag") void containsRecordingTag() {
        given().get("/v3/api-docs").then().statusCode(200).body("tags.name", hasItem("Recording"));
    }
    @Test @Order(5) @DisplayName("OpenAPI spec should document all admin mapping paths") void containsAdminMappingPaths() {
        given().get("/v3/api-docs").then().statusCode(200).body("paths.'/wixy/admin/mappings'", notNullValue()).body("paths.'/wixy/admin/mappings/{id}'", notNullValue());
    }
    @Test @Order(6) @DisplayName("GET /swagger-ui.html should redirect to Swagger UI") void swaggerUiRedirect() {
        given().redirects().follow(false).get("/swagger-ui.html").then().statusCode(anyOf(equalTo(200), equalTo(302)));
    }
}
