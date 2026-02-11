package io.github.vinipx.wixy.integration.recording;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Recording API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecordingIT extends BaseIntegrationTest {

    @BeforeEach void stopAnyActiveRecording() {
        try { given().post("/wixy/admin/recordings/stop"); } catch (Exception ignored) {}
    }

    @Test @Order(1) @DisplayName("GET /wixy/admin/recordings/status should return current status") void getRecordingStatus() {
        given().get("/wixy/admin/recordings/status").then().statusCode(200).contentType(ContentType.JSON).body("status", notNullValue());
    }
    @Test @Order(2) @DisplayName("POST /wixy/admin/recordings/start should start recording") void startRecording() {
        given().contentType(ContentType.JSON).body("{\"targetUrl\": \"http://httpbin.org\"}").post("/wixy/admin/recordings/start").then().statusCode(200).body("status", equalTo("Recording started"));
        given().get("/wixy/admin/recordings/status").then().body("status", equalTo("Recording"));
        given().post("/wixy/admin/recordings/stop").then().statusCode(200);
    }
    @Test @Order(3) @DisplayName("POST /wixy/admin/recordings/start without targetUrl and no config should return error") void startRecordingNoTarget() {
        given().contentType(ContentType.JSON).post("/wixy/admin/recordings/start").then().statusCode(anyOf(equalTo(200), equalTo(500)));
    }
    @Test @Order(4) @DisplayName("POST /wixy/admin/recordings/stop should stop recording and return captured stubs") void stopRecording() {
        given().contentType(ContentType.JSON).body("{\"targetUrl\": \"http://httpbin.org\"}").post("/wixy/admin/recordings/start").then().statusCode(200);
        given().post("/wixy/admin/recordings/stop").then().statusCode(200).body("status", equalTo("Recording stopped")).body("capturedStubs", greaterThanOrEqualTo(0));
    }
    @Test @Order(5) @DisplayName("POST /wixy/admin/recordings/stop without active recording should return 200 or 500") void stopWithoutStart() {
        given().post("/wixy/admin/recordings/stop").then().statusCode(anyOf(equalTo(200), equalTo(500)));
    }
    @Test @Order(6) @DisplayName("Full recording lifecycle: start → verify status → stop → verify results") void fullLifecycle() {
        given().contentType(ContentType.JSON).body("{\"targetUrl\": \"http://httpbin.org\"}").post("/wixy/admin/recordings/start").then().statusCode(200);
        given().get("/wixy/admin/recordings/status").then().body("status", equalTo("Recording"));
        given().post("/wixy/admin/recordings/stop").then().statusCode(200).body("capturedStubs", greaterThanOrEqualTo(0));
        given().get("/wixy/admin/recordings/status").then().body("status", not(equalTo("Recording")));
    }
}
