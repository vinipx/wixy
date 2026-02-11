package io.github.vinipx.wixy.integration.stub;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Stub Management API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StubManagementIT extends BaseIntegrationTest {

    @BeforeEach void resetState() { resetAllStubs(); }

    @Test @Order(1) @DisplayName("GET /wixy/admin/mappings should return empty list initially") void listAllEmpty() {
        given().get("/wixy/admin/mappings").then().statusCode(200).contentType(ContentType.JSON).body("meta.total", equalTo(0));
    }
    @Test @Order(2) @DisplayName("POST /wixy/admin/mappings should create a stub and return 201") void createStubMapping() {
        given().contentType(ContentType.JSON).body(TEST_STUB_JSON).post("/wixy/admin/mappings").then().statusCode(201).body(containsString("integration-test"));
    }
    @Test @Order(3) @DisplayName("POST /wixy/admin/mappings with invalid JSON should return 400") void createInvalidStub() {
        given().contentType(ContentType.JSON).body("not valid json").post("/wixy/admin/mappings").then().statusCode(400).body("error", equalTo("Bad Request"));
    }
    @Test @Order(4) @DisplayName("GET /wixy/admin/mappings should return stubs after creation") void listAfterCreate() {
        createStub(TEST_STUB_JSON);
        given().get("/wixy/admin/mappings").then().statusCode(200).body("meta.total", equalTo(1));
    }
    @Test @Order(5) @DisplayName("GET /wixy/admin/mappings/{id} should return the specific stub") void getById() {
        String body = createStub(TEST_STUB_JSON);
        String id = io.restassured.path.json.JsonPath.from(body).getString("id");
        given().get("/wixy/admin/mappings/{id}", id).then().statusCode(200).body(containsString(id));
    }
    @Test @Order(6) @DisplayName("GET /wixy/admin/mappings/{id} with unknown ID should return 404") void getByIdNotFound() {
        given().get("/wixy/admin/mappings/{id}", "00000000-0000-0000-0000-000000000000").then().statusCode(404).body("error", equalTo("Not Found"));
    }
    @Test @Order(7) @DisplayName("PUT /wixy/admin/mappings/{id} should update an existing stub") void updateStub() {
        String body = createStub(TEST_STUB_JSON);
        String id = io.restassured.path.json.JsonPath.from(body).getString("id");
        given().contentType(ContentType.JSON).body(UPDATED_STUB_JSON).put("/wixy/admin/mappings/{id}", id).then().statusCode(200).body(containsString("integration-updated"));
    }
    @Test @Order(8) @DisplayName("PUT /wixy/admin/mappings/{id} with unknown ID should return 404") void updateNotFound() {
        given().contentType(ContentType.JSON).body(TEST_STUB_JSON).put("/wixy/admin/mappings/{id}", "00000000-0000-0000-0000-000000000000").then().statusCode(404);
    }
    @Test @Order(9) @DisplayName("DELETE /wixy/admin/mappings/{id} should remove the stub") void deleteStub() {
        String body = createStub(TEST_STUB_JSON);
        String id = io.restassured.path.json.JsonPath.from(body).getString("id");
        given().delete("/wixy/admin/mappings/{id}", id).then().statusCode(204);
        given().get("/wixy/admin/mappings/{id}", id).then().statusCode(404);
    }
    @Test @Order(10) @DisplayName("DELETE /wixy/admin/mappings/{id} with unknown ID should return 404") void deleteNotFound() {
        given().delete("/wixy/admin/mappings/{id}", "00000000-0000-0000-0000-000000000000").then().statusCode(404);
    }
    @Test @Order(11) @DisplayName("POST /wixy/admin/mappings/reset should clear all stubs") void resetAll() {
        createStub(TEST_STUB_JSON); createStub(UPDATED_STUB_JSON);
        given().post("/wixy/admin/mappings/reset").then().statusCode(200).body("status", equalTo("All mappings reset"));
        given().get("/wixy/admin/mappings").then().body("meta.total", equalTo(0));
    }
    @Test @Order(12) @DisplayName("POST /wixy/admin/mappings/import should import stubs") void importStubs() {
        given().contentType(ContentType.JSON).body(TEST_STUB_JSON).post("/wixy/admin/mappings/import").then().statusCode(200).body("imported", equalTo(1));
    }
    @Test @Order(13) @DisplayName("Full CRUD lifecycle: create → read → update → delete") void fullLifecycle() {
        String body = createStub(TEST_STUB_JSON);
        String id = io.restassured.path.json.JsonPath.from(body).getString("id");
        given().get("/wixy/admin/mappings/{id}", id).then().statusCode(200);
        given().contentType(ContentType.JSON).body(UPDATED_STUB_JSON).put("/wixy/admin/mappings/{id}", id).then().statusCode(200);
        given().delete("/wixy/admin/mappings/{id}", id).then().statusCode(204);
        given().get("/wixy/admin/mappings/{id}", id).then().statusCode(404);
    }
}
