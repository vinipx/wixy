package io.github.vinipx.wixy.integration.proxy;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Proxy Management API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProxyManagementIT extends BaseIntegrationTest {

    @BeforeEach void resetProxy() { given().post("/wixy/admin/proxy/disable").then().statusCode(200); }

    @Test @Order(1) @DisplayName("GET /wixy/admin/proxy should return current proxy status") void getProxyStatus() {
        given().get("/wixy/admin/proxy").then().statusCode(200).contentType(ContentType.JSON).body("enabled", equalTo(false)).body("wiremockPort", greaterThan(0));
    }
    @Test @Order(2) @DisplayName("POST /wixy/admin/proxy/enable should enable proxy to target URL") void enableProxy() {
        given().contentType(ContentType.JSON).body("{\"targetUrl\": \"http://httpbin.org\"}").post("/wixy/admin/proxy/enable").then().statusCode(200).body("status", equalTo("Proxy enabled")).body("targetUrl", equalTo("http://httpbin.org"));
        given().get("/wixy/admin/proxy").then().body("enabled", equalTo(true)).body("targetUrl", equalTo("http://httpbin.org"));
    }
    @Test @Order(3) @DisplayName("POST /wixy/admin/proxy/enable with blank targetUrl should return 500") void enableProxyBlankTarget() {
        given().contentType(ContentType.JSON).body("{\"targetUrl\": \"\"}").post("/wixy/admin/proxy/enable").then().statusCode(500).body("message", containsString("Target URL must not be blank"));
    }
    @Test @Order(4) @DisplayName("POST /wixy/admin/proxy/enable with missing targetUrl should return 500") void enableProxyMissingTarget() {
        given().contentType(ContentType.JSON).body("{}").post("/wixy/admin/proxy/enable").then().statusCode(500);
    }
    @Test @Order(5) @DisplayName("POST /wixy/admin/proxy/disable should disable proxy") void disableProxy() {
        given().contentType(ContentType.JSON).body("{\"targetUrl\": \"http://httpbin.org\"}").post("/wixy/admin/proxy/enable").then().statusCode(200);
        given().post("/wixy/admin/proxy/disable").then().statusCode(200).body("status", equalTo("Proxy disabled"));
        given().get("/wixy/admin/proxy").then().body("enabled", equalTo(false));
    }
    @Test @Order(6) @DisplayName("POST /wixy/admin/proxy/disable should be idempotent") void disableProxyIdempotent() {
        given().post("/wixy/admin/proxy/disable").then().statusCode(200);
        given().post("/wixy/admin/proxy/disable").then().statusCode(200);
    }
    @Test @Order(7) @DisplayName("Full proxy lifecycle: check status → enable → verify → disable → verify") void fullLifecycle() {
        given().get("/wixy/admin/proxy").then().body("enabled", equalTo(false));
        given().contentType(ContentType.JSON).body("{\"targetUrl\": \"http://httpbin.org\"}").post("/wixy/admin/proxy/enable").then().statusCode(200);
        given().get("/wixy/admin/proxy").then().body("enabled", equalTo(true));
        given().post("/wixy/admin/proxy/disable").then().statusCode(200);
        given().get("/wixy/admin/proxy").then().body("enabled", equalTo(false));
    }
}
