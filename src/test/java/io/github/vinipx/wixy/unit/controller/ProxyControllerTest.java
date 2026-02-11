package io.github.vinipx.wixy.unit.controller;

import io.github.vinipx.wixy.controller.ProxyController;
import io.github.vinipx.wixy.exception.WixyException;
import io.github.vinipx.wixy.service.ProxyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("ProxyController")
class ProxyControllerTest {

    private final ProxyService proxyService = mock(ProxyService.class);
    private final ProxyController controller = new ProxyController(proxyService);

    @Nested @DisplayName("GET /wixy/admin/proxy") class GetStatus {
        @Test @DisplayName("should return 200 with proxy status") void success() {
            Map<String, Object> statusMap = Map.of("enabled", false, "targetUrl", "", "record", false, "wiremockPort", 9090);
            when(proxyService.getStatus()).thenReturn(statusMap);
            var response = controller.getStatus();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("enabled", false).containsEntry("wiremockPort", 9090);
        }
    }

    @Nested @DisplayName("POST /wixy/admin/proxy/enable") class Enable {
        @Test @DisplayName("should return 200 with status and targetUrl") void success() {
            doNothing().when(proxyService).enableProxy("http://backend:8080");
            var body = Map.of("targetUrl", "http://backend:8080");
            var response = controller.enable(body);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("status", "Proxy enabled").containsEntry("targetUrl", "http://backend:8080");
            verify(proxyService).enableProxy("http://backend:8080");
        }
        @Test @DisplayName("should use empty string when targetUrl is missing from body") void missingTargetUrl() {
            doThrow(new WixyException("Target URL must not be blank")).when(proxyService).enableProxy("");
            assertThatThrownBy(() -> controller.enable(Map.of())).isInstanceOf(WixyException.class);
        }
    }

    @Nested @DisplayName("POST /wixy/admin/proxy/disable") class Disable {
        @Test @DisplayName("should return 200 with disabled status") void success() {
            doNothing().when(proxyService).disableProxy();
            var response = controller.disable();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("status", "Proxy disabled");
            verify(proxyService).disableProxy();
        }
    }
}
