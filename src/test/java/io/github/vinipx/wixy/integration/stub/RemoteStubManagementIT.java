package io.github.vinipx.wixy.integration.stub;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration test to guarantee that a user is able to manage Stubs (Create, Read, Edit, Delete)
 * for a remote WireMock engine via the Wixy Hub.
 */
@Tag("integration")
@DisplayName("Remote Engine Stub Management API")
class RemoteStubManagementIT extends BaseIntegrationTest {

    private static final int REMOTE_PORT = 9910;
    private static WireMockServer remoteServer;
    private String remoteServerId;

    @BeforeAll
    static void startRemoteMock() {
        remoteServer = new WireMockServer(REMOTE_PORT);
        remoteServer.start();
    }

    @AfterAll
    static void stopRemoteMock() {
        if (remoteServer != null) {
            remoteServer.stop();
        }
    }

    @BeforeEach
    void setupRemoteServerInHub() {
        // 1. Register the remote server in the Hub
        String body = given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Remote-Stub-Test", "url", "http://localhost:" + REMOTE_PORT))
                .post("/wixy/admin/registry/servers")
                .then().statusCode(201).extract().asString();
        
        remoteServerId = io.restassured.path.json.JsonPath.from(body).getString("id");

        // 2. Set it as the active engine
        given().contentType(ContentType.JSON).body(Map.of("id", remoteServerId))
                .post("/wixy/admin/registry/active").then().statusCode(200);

        // 3. Reset stubs on the remote engine to ensure a clean state
        given().post("/wixy/admin/mappings/reset").then().statusCode(200);
    }

    @Test
    @DisplayName("Should successfully Create, Read, Update, and Delete a stub on a remote engine")
    void fullStubLifecycleOnRemoteEngine() {
        String testStubPath = "/remote-lifecycle-test";

        // 1. Create a new stub on the remote engine
        String createJson = """
                {
                  "request": { "method": "GET", "url": "%s" },
                  "response": { "status": 200, "body": "remote-created" }
                }
                """.formatted(testStubPath);

        String createResponse = given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .post("/wixy/admin/mappings")
                .then().log().ifValidationFails().statusCode(201)
                .body(containsString("remote-created"))
                .extract().asString();

        String stubId = io.restassured.path.json.JsonPath.from(createResponse).getString("id");
        assertThat(stubId, notNullValue());

        // 2. Read the specific stub from the remote engine
        given()
                .get("/wixy/admin/mappings/" + stubId)
                .then().log().ifValidationFails().statusCode(200)
                .body("response.body", equalTo("remote-created"));

        // 3. Update the stub on the remote engine
        String updateJson = """
                {
                  "request": { "method": "GET", "url": "%s" },
                  "response": { "status": 202, "body": "remote-updated" }
                }
                """.formatted(testStubPath);

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .put("/wixy/admin/mappings/" + stubId)
                .then().log().ifValidationFails().statusCode(200)
                .body("response.body", equalTo("remote-updated"));

        // Verify the update via Read
        given()
                .get("/wixy/admin/mappings/" + stubId)
                .then().log().ifValidationFails().statusCode(200)
                .body("response.status", equalTo(202))
                .body("response.body", equalTo("remote-updated"));

        // 4. Delete the stub from the remote engine
        given()
                .delete("/wixy/admin/mappings/" + stubId)
                .then().log().ifValidationFails().statusCode(204);

        // Verify it is deleted
        given()
                .get("/wixy/admin/mappings/" + stubId)
                .then().log().ifValidationFails().statusCode(404);
                
        // Verify list is empty
        given()
                .get("/wixy/admin/mappings")
                .then().log().ifValidationFails().statusCode(200)
                .body("meta.total", equalTo(0));
    }

    @Test
    @DisplayName("Should support direct targeting via X-Wixy-Target-Server header for remote engine")
    void directTargetingRemoteStubLifecycle() {
        // Switch back to local engine to ensure header targeting works
        given().contentType(ContentType.JSON).body(Map.of("id", "local"))
                .post("/wixy/admin/registry/active").then().log().ifValidationFails().statusCode(200);

        String testStubPath = "/remote-direct-test";

        String createJson = """
                {
                  "request": { "method": "GET", "url": "%s" },
                  "response": { "status": 200, "body": "direct-remote" }
                }
                """.formatted(testStubPath);

        // Create stub on remote server using header
        String createResponse = given()
                .header("X-Wixy-Target-Server", remoteServerId)
                .contentType(ContentType.JSON)
                .body(createJson)
                .post("/wixy/admin/mappings")
                .then().log().ifValidationFails().statusCode(201)
                .extract().asString();

        String stubId = io.restassured.path.json.JsonPath.from(createResponse).getString("id");

        // The local engine should not have this stub
        given()
                .get("/wixy/admin/mappings")
                .then().log().ifValidationFails().statusCode(200)
                .body("mappings.request.url", not(hasItem(testStubPath)));

        // Read specific stub from remote engine using header
        given()
                .header("X-Wixy-Target-Server", remoteServerId)
                .get("/wixy/admin/mappings/" + stubId)
                .then().log().ifValidationFails().statusCode(200)
                .body("response.body", equalTo("direct-remote"));
                
        // Delete the stub from remote engine using header
        given()
                .header("X-Wixy-Target-Server", remoteServerId)
                .delete("/wixy/admin/mappings/" + stubId)
                .then().log().ifValidationFails().statusCode(204);
    }
    
    // Helper assert method for stubId since we use RestAssured matcher for it
    private void assertThat(String actual, org.hamcrest.Matcher<Object> matcher) {
        org.hamcrest.MatcherAssert.assertThat(actual, matcher);
    }
}
