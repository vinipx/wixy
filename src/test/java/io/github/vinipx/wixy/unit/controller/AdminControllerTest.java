package io.github.vinipx.wixy.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.controller.AdminController;
import io.github.vinipx.wixy.exception.InvalidStubDefinitionException;
import io.github.vinipx.wixy.exception.StubNotFoundException;
import io.github.vinipx.wixy.service.StubService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("AdminController")
class AdminControllerTest {

    private final StubService stubService = mock(StubService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AdminController controller = new AdminController(stubService, objectMapper);

    @Nested @DisplayName("GET /wixy/admin/mappings") class ListAll {
        @Test @DisplayName("should return 200 with empty list when no stubs") void emptyList() {
            when(stubService.listAll()).thenReturn(List.of());
            var response = controller.listAll();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            @SuppressWarnings("unchecked") var body = (Map<String, Object>) response.getBody();
            assertThat(body).containsKey("mappings").containsKey("meta");
            @SuppressWarnings("unchecked") var meta = (Map<String, Object>) body.get("meta");
            assertThat(meta).containsEntry("total", 0);
        }
        @Test @DisplayName("should return 200 with stub list when stubs exist") void withStubs() {
            var stub1 = mock(StubMapping.class); var stub2 = mock(StubMapping.class);
            when(stub1.toString()).thenReturn("{\"id\":\"1\"}"); when(stub2.toString()).thenReturn("{\"id\":\"2\"}");
            when(stubService.listAll()).thenReturn(List.of(stub1, stub2));
            var response = controller.listAll();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            @SuppressWarnings("unchecked") var body = (Map<String, Object>) response.getBody();
            @SuppressWarnings("unchecked") var meta = (Map<String, Object>) body.get("meta");
            assertThat(meta).containsEntry("total", 2);
        }
        @Test @DisplayName("should delegate to StubService.listAll()") void delegatesToService() {
            when(stubService.listAll()).thenReturn(List.of()); controller.listAll(); verify(stubService).listAll();
        }
    }

    @Nested @DisplayName("POST /wixy/admin/mappings") class Create {
        @Test @DisplayName("should return 201 with created stub") void success() {
            var stub = mock(StubMapping.class); when(stub.toString()).thenReturn("{\"id\":\"abc\"}");
            when(stubService.create("json-input")).thenReturn(stub);
            var response = controller.create("json-input");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().toString()).contains("abc");
        }
        @Test @DisplayName("should propagate InvalidStubDefinitionException") void invalidInput() {
            when(stubService.create("bad")).thenThrow(new InvalidStubDefinitionException("parse error"));
            assertThatThrownBy(() -> controller.create("bad")).isInstanceOf(InvalidStubDefinitionException.class);
        }
    }

    @Nested @DisplayName("GET /wixy/admin/mappings/{id}") class GetById {
        @Test @DisplayName("should return 200 with stub") void found() {
            UUID id = UUID.randomUUID(); var stub = mock(StubMapping.class);
            when(stub.toString()).thenReturn("{\"id\":\"" + id + "\"}"); when(stubService.getById(id)).thenReturn(stub);
            var response = controller.getById(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().toString()).contains(id.toString());
        }
        @Test @DisplayName("should propagate StubNotFoundException") void notFound() {
            UUID id = UUID.randomUUID(); when(stubService.getById(id)).thenThrow(new StubNotFoundException(id.toString()));
            assertThatThrownBy(() -> controller.getById(id)).isInstanceOf(StubNotFoundException.class);
        }
    }

    @Nested @DisplayName("PUT /wixy/admin/mappings/{id}") class Update {
        @Test @DisplayName("should return 200 with updated stub") void success() {
            UUID id = UUID.randomUUID(); var stub = mock(StubMapping.class);
            when(stub.toString()).thenReturn("{\"updated\":true}"); when(stubService.update(id, "json")).thenReturn(stub);
            var response = controller.update(id, "json");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); assertThat(response.getBody().toString()).contains("updated");
        }
        @Test @DisplayName("should propagate StubNotFoundException for unknown ID") void notFound() {
            UUID id = UUID.randomUUID(); when(stubService.update(eq(id), any())).thenThrow(new StubNotFoundException(id.toString()));
            assertThatThrownBy(() -> controller.update(id, "json")).isInstanceOf(StubNotFoundException.class);
        }
    }

    @Nested @DisplayName("DELETE /wixy/admin/mappings/{id}") class Delete {
        @Test @DisplayName("should return 204 No Content") void success() {
            UUID id = UUID.randomUUID(); doNothing().when(stubService).delete(id);
            var response = controller.delete(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull(); verify(stubService).delete(id);
        }
        @Test @DisplayName("should propagate StubNotFoundException") void notFound() {
            UUID id = UUID.randomUUID(); doThrow(new StubNotFoundException(id.toString())).when(stubService).delete(id);
            assertThatThrownBy(() -> controller.delete(id)).isInstanceOf(StubNotFoundException.class);
        }
    }

    @Nested @DisplayName("POST /wixy/admin/mappings/reset") class ResetAll {
        @Test @DisplayName("should return 200 with status message") void success() {
            doNothing().when(stubService).resetAll(); var response = controller.resetAll();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            @SuppressWarnings("unchecked") var body = (Map<String, String>) response.getBody();
            assertThat(body).containsEntry("status", "All mappings reset"); verify(stubService).resetAll();
        }
    }

    @Nested @DisplayName("POST /wixy/admin/mappings/import") class ImportStubs {
        @Test @DisplayName("should return 200 with imported count") void success() {
            when(stubService.importStubs("json")).thenReturn(5); var response = controller.importStubs("json");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            @SuppressWarnings("unchecked") var body = (Map<String, Object>) response.getBody();
            assertThat(body).containsEntry("imported", 5);
        }
    }
}
