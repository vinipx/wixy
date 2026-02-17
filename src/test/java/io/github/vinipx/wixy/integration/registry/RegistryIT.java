package io.github.vinipx.wixy.integration.registry;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@DisplayName("Registry Management API")
class RegistryIT extends BaseIntegrationTest {

    private static WireMockServer dummyRemote;
    private static final int DUMMY_PORT = 9920;

    @BeforeAll
    static void startDummy() {
        dummyRemote = new WireMockServer(DUMMY_PORT);
        dummyRemote.start();
    }

    @AfterAll
    static void stopDummy() {
        if (dummyRemote != null) dummyRemote.stop();
    }

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
        // 1. Add a remote server (the one we just started)
        String body = given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Integration Test Remote", "url", "http://localhost:" + DUMMY_PORT))
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
