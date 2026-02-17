package io.github.vinipx.wixy.integration.registry;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@DisplayName("Direct Target Routing")
class TargetRoutingIT extends BaseIntegrationTest {

    private static WireMockServer remoteServer;
    private static final int REMOTE_PORT = 9910;
    private String remoteId;

    @BeforeAll
    static void startRemote() {
        remoteServer = new WireMockServer(REMOTE_PORT);
        remoteServer.start();
        remoteServer.stubFor(get(urlEqualTo("/remote")).willReturn(ok("remote-response")));
    }

    @AfterAll
    static void stopRemote() {
        if (remoteServer != null) remoteServer.stop();
    }

    @BeforeEach
    void registerRemote() {
        String body = given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Target-Test", "url", "http://localhost:" + REMOTE_PORT))
                .post("/wixy/admin/registry/servers")
                .then().statusCode(201).extract().asString();
        remoteId = io.restassured.path.json.JsonPath.from(body).getString("id");
    }

    @Test
    @DisplayName("Should route to remote server when X-Wixy-Target-Server header is present")
    void directTargeting() {
        // 1. Request without header -> hits LOCAL (empty)
        given()
                .get("/wixy/admin/mappings")
                .then()
                .statusCode(200)
                .body("mappings.request.url", not(hasItem("/remote")));

        // 2. Request with header -> hits REMOTE (has /remote stub)
        given()
                .header("X-Wixy-Target-Server", remoteId)
                .get("/wixy/admin/mappings")
                .then()
                .statusCode(200)
                .body("mappings.request.url", hasItem("/remote"));

        // 3. Subsequent request without header -> still hits LOCAL (proves it's per-request)
        given()
                .get("/wixy/admin/mappings")
                .then()
                .statusCode(200)
                .body("mappings.request.url", not(hasItem("/remote")));
    }
}
