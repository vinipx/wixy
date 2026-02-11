package io.github.vinipx.wixy.unit.exception;

import io.github.vinipx.wixy.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Tag("unit")
@DisplayName("Exception handling")
class ExceptionTest {

    @Nested @DisplayName("WixyException") class WixyExceptionTests {
        @Test @DisplayName("should carry message") void message() { assertThat(new WixyException("something failed").getMessage()).isEqualTo("something failed"); }
        @Test @DisplayName("should carry message and cause") void messageAndCause() {
            var cause = new RuntimeException("root cause"); var ex = new WixyException("wrapped", cause);
            assertThat(ex.getMessage()).isEqualTo("wrapped"); assertThat(ex.getCause()).isEqualTo(cause);
        }
        @Test @DisplayName("should be a RuntimeException") void isRuntimeException() { assertThat(new WixyException("test")).isInstanceOf(RuntimeException.class); }
    }

    @Nested @DisplayName("StubNotFoundException") class StubNotFoundExceptionTests {
        @Test @DisplayName("should include stub ID in message") void messageContainsId() {
            var ex = new StubNotFoundException("abc-123");
            assertThat(ex.getMessage()).contains("abc-123").contains("Stub mapping not found");
        }
        @Test @DisplayName("should extend WixyException") void extendsWixyException() { assertThat(new StubNotFoundException("id")).isInstanceOf(WixyException.class); }
    }

    @Nested @DisplayName("InvalidStubDefinitionException") class InvalidStubDefinitionExceptionTests {
        @Test @DisplayName("should carry message") void message() { assertThat(new InvalidStubDefinitionException("bad json").getMessage()).isEqualTo("bad json"); }
        @Test @DisplayName("should carry message and cause") void messageAndCause() {
            var cause = new RuntimeException("parse error"); var ex = new InvalidStubDefinitionException("failed", cause);
            assertThat(ex.getMessage()).isEqualTo("failed"); assertThat(ex.getCause()).isEqualTo(cause);
        }
        @Test @DisplayName("should extend WixyException") void extendsWixyException() { assertThat(new InvalidStubDefinitionException("x")).isInstanceOf(WixyException.class); }
    }

    @Nested @DisplayName("GlobalExceptionHandler") class GlobalExceptionHandlerTests {
        private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

        @Test @DisplayName("should handle StubNotFoundException as 404") void stubNotFound() {
            var response = handler.handleStubNotFound(new StubNotFoundException("missing-id"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).containsEntry("status", 404).containsEntry("error", "Not Found");
            assertThat((String) response.getBody().get("message")).contains("missing-id");
            assertThat(response.getBody()).containsKey("timestamp");
        }
        @Test @DisplayName("should handle InvalidStubDefinitionException as 400") void invalidStub() {
            var response = handler.handleInvalidStub(new InvalidStubDefinitionException("bad format"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("status", 400).containsEntry("error", "Bad Request");
            assertThat((String) response.getBody().get("message")).contains("bad format");
        }
        @Test @DisplayName("should handle WixyException as 500") void wixyException() {
            var response = handler.handleWixyException(new WixyException("internal problem"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).containsEntry("status", 500).containsEntry("error", "Internal Server Error");
        }
        @Test @DisplayName("should handle generic Exception as 500") void genericException() {
            var response = handler.handleGeneric(new RuntimeException("unexpected"));
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat((String) response.getBody().get("message")).isEqualTo("An unexpected error occurred");
        }
        @Test @DisplayName("all responses should include timestamp") void timestampPresent() {
            assertThat(handler.handleStubNotFound(new StubNotFoundException("x")).getBody().get("timestamp")).isNotNull();
            assertThat(handler.handleInvalidStub(new InvalidStubDefinitionException("x")).getBody().get("timestamp")).isNotNull();
            assertThat(handler.handleWixyException(new WixyException("x")).getBody().get("timestamp")).isNotNull();
            assertThat(handler.handleGeneric(new RuntimeException("x")).getBody().get("timestamp")).isNotNull();
        }
        @Test @DisplayName("all responses should have consistent structure") void consistentStructure() {
            Map<String, Object> body = handler.handleStubNotFound(new StubNotFoundException("x")).getBody();
            assertThat(body).containsKeys("timestamp", "status", "error", "message");
        }
    }
}
