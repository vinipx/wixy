package io.github.vinipx.wixy.integration.registry;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration test that simulates multiple remote WireMock servers
 * to validate the Hub's multi-server management functionality.
 */
@Tag("integration")
@DisplayName("Remote Engine Simulation")
class RemoteManagementIT extends BaseIntegrationTest {

    private static final int[] REMOTE_PORTS = {9901, 9902, 9903};
    private static final WireMockServer[] remoteServers = new WireMockServer[3];

    @BeforeAll
    static void startRemoteMocks() {
        for (int i = 0; i < REMOTE_PORTS.length; i++) {
            remoteServers[i] = new WireMockServer(REMOTE_PORTS[i]);
            remoteServers[i].start();
            
            // Give each server a unique identity stub
            remoteServers[i].stubFor(get(urlEqualTo("/identity"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody("{\"serverId\": \"server-" + (i + 1) + "\"}")));
        }
    }

    @AfterAll
    static void stopRemoteMocks() {
        for (WireMockServer server : remoteServers) {
            if (server != null) server.stop();
        }
    }

    @Test
    @DisplayName("Wixy Hub should manage 3 independent remote servers and register unique stubs on each")
    void manageThreeServers() {
        String[] serverIds = new String[3];

        // 1. Register all 3 servers in the Hub
        for (int i = 0; i < 3; i++) {
            String body = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Remote-" + (i + 1), "url", "http://localhost:" + REMOTE_PORTS[i]))
                    .post("/wixy/admin/registry/servers")
                    .then().statusCode(201).extract().asString();
            serverIds[i] = io.restassured.path.json.JsonPath.from(body).getString("id");
        }

        // 2. Register unique stubs on each by switching context
        for (int i = 0; i < 3; i++) {
            String targetId = serverIds[i];
            String uniquePath = "/stub-for-server-" + (i + 1);

            // Switch Focus
            given().contentType(ContentType.JSON).body(Map.of("id", targetId))
                    .post("/wixy/admin/registry/active").then().statusCode(200);

            // Register Stub
            given().contentType(ContentType.JSON)
                    .body("{\"request\": {\"method\": \"GET\", \"url\": \"" + uniquePath + "\"}, \"response\": {\"status\": 200}}")
                    .post("/wixy/admin/mappings").then().statusCode(201);
        }

        // 3. Verify Isolation: Each server should only have its own stub
        for (int i = 0; i < 3; i++) {
            // Switch Focus
            given().contentType(ContentType.JSON).body(Map.of("id", serverIds[i]))
                    .post("/wixy/admin/registry/active").then().statusCode(200);

            // Check Mappings
            given().get("/wixy/admin/mappings").then()
                    .statusCode(200)
                    .body("mappings.request.url", hasItem("/stub-for-server-" + (i + 1)))
                    .body("mappings.request.url", not(hasItem("/stub-for-server-" + (((i + 1) % 3) + 1))));
        }
    }
}
