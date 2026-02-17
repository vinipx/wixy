package io.github.vinipx.wixy.integration.registry;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@DisplayName("Registry Management API")
class RegistryIT extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /wixy/admin/registry/servers should return list with local server")
    void listServers() {
        given()
                .get("/wixy/admin/registry/servers")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].name", containsString("Local"));
    }

    @Test
    @DisplayName("Full registry workflow: add → switch → list → remove")
    void registryWorkflow() {
        // 1. Add a remote server
        String body = given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Integration Test Remote", "url", "http://localhost:9999"))
                .post("/wixy/admin/registry/servers")
                .then()
                .statusCode(201)
                .extract().asString();
        
        String remoteId = io.restassured.path.json.JsonPath.from(body).getString("id");

        // 2. Switch to it
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("id", remoteId))
                .post("/wixy/admin/registry/active")
                .then()
                .statusCode(200)
                .body("activeServerId", equalTo(remoteId));

        // 3. Verify it is active
        given()
                .get("/wixy/admin/registry/active")
                .then()
                .statusCode(200)
                .body("activeServerId", equalTo(remoteId));

        // 4. Switch back to local
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("id", "local"))
                .post("/wixy/admin/registry/active")
                .then()
                .statusCode(200)
                .body("activeServerId", equalTo("local"));

        // 5. Remove it
        given()
                .delete("/wixy/admin/registry/servers/{id}", remoteId)
                .then()
                .statusCode(204);
    }
}
