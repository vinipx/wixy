package io.github.vinipx.wixy.unit.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.exception.InvalidStubDefinitionException;
import io.github.vinipx.wixy.exception.StubNotFoundException;
import io.github.vinipx.wixy.service.StubService;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Tag("unit")
@DisplayName("StubService")
class StubServiceTest {

    private static WireMockServer wireMockServer;
    private StubService stubService;

    private static final String VALID_STUB_JSON = """
            {
              "request": { "method": "GET", "urlPath": "/api/test" },
              "response": { "status": 200, "jsonBody": { "message": "hello" } }
            }
            """;

    private static final String VALID_STUB_JSON_2 = """
            {
              "request": { "method": "POST", "urlPath": "/api/other" },
              "response": { "status": 201, "jsonBody": { "created": true } }
            }
            """;

    @BeforeAll static void startServer() { wireMockServer = new WireMockServer(0); wireMockServer.start(); }
    @AfterAll static void stopServer() { if (wireMockServer != null && wireMockServer.isRunning()) wireMockServer.stop(); }
    @BeforeEach void setUp() { wireMockServer.resetMappings(); stubService = new StubService(wireMockServer); }

    @Nested @DisplayName("listAll()") class ListAll {
        @Test @DisplayName("should return empty list when no stubs configured") void emptyList() { assertThat(stubService.listAll()).isEmpty(); }
        @Test @DisplayName("should return all configured stubs") void withStubs() {
            stubService.create(VALID_STUB_JSON); stubService.create(VALID_STUB_JSON_2);
            assertThat(stubService.listAll()).hasSize(2);
        }
    }

    @Nested @DisplayName("create()") class Create {
        @Test @DisplayName("should create a valid stub mapping") void validStub() {
            StubMapping created = stubService.create(VALID_STUB_JSON);
            assertThat(created).isNotNull(); assertThat(created.getId()).isNotNull(); assertThat(stubService.listAll()).hasSize(1);
        }
        @Test @DisplayName("should assign a UUID to the created stub") void hasUUID() {
            assertThat(stubService.create(VALID_STUB_JSON).getId()).isInstanceOf(UUID.class);
        }
        @Test @DisplayName("should throw InvalidStubDefinitionException for malformed JSON") void invalidJson() {
            assertThatThrownBy(() -> stubService.create("not json")).isInstanceOf(InvalidStubDefinitionException.class).hasMessageContaining("Failed to parse stub definition");
        }
        @Test @DisplayName("should throw InvalidStubDefinitionException for empty input") void emptyInput() {
            assertThatThrownBy(() -> stubService.create("")).isInstanceOf(InvalidStubDefinitionException.class);
        }
        @Test @DisplayName("should accept minimal JSON object (WireMock is lenient)") void minimalJsonObject() {
            assertThat(stubService.create("{}")).isNotNull();
        }
    }

    @Nested @DisplayName("getById()") class GetById {
        @Test @DisplayName("should return stub by its UUID") void existingStub() {
            StubMapping created = stubService.create(VALID_STUB_JSON);
            StubMapping fetched = stubService.getById(created.getId());
            assertThat(fetched).isNotNull(); assertThat(fetched.getId()).isEqualTo(created.getId());
        }
        @Test @DisplayName("should throw StubNotFoundException for unknown UUID") void unknownId() {
            UUID fakeId = UUID.randomUUID();
            assertThatThrownBy(() -> stubService.getById(fakeId)).isInstanceOf(StubNotFoundException.class).hasMessageContaining(fakeId.toString());
        }
    }

    @Nested @DisplayName("update()") class Update {
        @Test @DisplayName("should update an existing stub mapping") void existingStub() {
            StubMapping created = stubService.create(VALID_STUB_JSON);
            String updatedJson = """
                    { "request": { "method": "GET", "urlPath": "/api/updated" }, "response": { "status": 200, "jsonBody": { "message": "updated" } } }
                    """;
            StubMapping updated = stubService.update(created.getId(), updatedJson);
            assertThat(updated).isNotNull(); assertThat(updated.getId()).isEqualTo(created.getId());
        }
        @Test @DisplayName("should throw StubNotFoundException when updating non-existent stub") void nonExistent() {
            assertThatThrownBy(() -> stubService.update(UUID.randomUUID(), VALID_STUB_JSON)).isInstanceOf(StubNotFoundException.class);
        }
        @Test @DisplayName("should throw InvalidStubDefinitionException for malformed update JSON") void invalidUpdateJson() {
            StubMapping created = stubService.create(VALID_STUB_JSON);
            assertThatThrownBy(() -> stubService.update(created.getId(), "not json")).isInstanceOf(InvalidStubDefinitionException.class);
        }
    }

    @Nested @DisplayName("delete()") class Delete {
        @Test @DisplayName("should delete an existing stub mapping") void existingStub() {
            StubMapping created = stubService.create(VALID_STUB_JSON);
            assertThat(stubService.listAll()).hasSize(1); stubService.delete(created.getId()); assertThat(stubService.listAll()).isEmpty();
        }
        @Test @DisplayName("should throw StubNotFoundException when deleting non-existent stub") void nonExistent() {
            assertThatThrownBy(() -> stubService.delete(UUID.randomUUID())).isInstanceOf(StubNotFoundException.class);
        }
    }

    @Nested @DisplayName("resetAll()") class ResetAll {
        @Test @DisplayName("should remove all stub mappings") void clearsAll() {
            stubService.create(VALID_STUB_JSON); stubService.create(VALID_STUB_JSON_2);
            assertThat(stubService.listAll()).hasSize(2); stubService.resetAll(); assertThat(stubService.listAll()).isEmpty();
        }
        @Test @DisplayName("should be safe to call when no stubs exist") void emptyReset() {
            assertThatCode(() -> stubService.resetAll()).doesNotThrowAnyException(); assertThat(stubService.listAll()).isEmpty();
        }
    }

    @Nested @DisplayName("importStubs()") class ImportStubs {
        @Test @DisplayName("should import a single stub from JSON") void singleStub() {
            int count = stubService.importStubs(VALID_STUB_JSON);
            assertThat(count).isEqualTo(1); assertThat(stubService.listAll()).hasSize(1);
        }
        @Test @DisplayName("should throw InvalidStubDefinitionException for malformed import JSON") void malformedImport() {
            assertThatThrownBy(() -> stubService.importStubs("broken json")).isInstanceOf(InvalidStubDefinitionException.class).hasMessageContaining("Failed to import stubs");
        }
    }
}
