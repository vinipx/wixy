package io.github.vinipx.wixy.unit.application;

import io.github.vinipx.wixy.WixyApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Basic smoke test for {@link WixyApplication} — verifies the main class exists
 * and the main method can be invoked without full context loading.
 */
@Tag("unit")
@DisplayName("WixyApplication")
class WixyApplicationTest {

    @Test
    @DisplayName("should have a main method")
    void hasMainMethod() throws NoSuchMethodException {
        var method = WixyApplication.class.getMethod("main", String[].class);
        assertThat(method).isNotNull();
    }

    @Test
    @DisplayName("should be annotated with @SpringBootApplication")
    void hasSpringBootAnnotation() {
        assertThat(WixyApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class
        )).isTrue();
    }

    @Test
    @DisplayName("should be annotated with @EnableConfigurationProperties")
    void hasConfigurationPropertiesAnnotation() {
        assertThat(WixyApplication.class.isAnnotationPresent(
                org.springframework.boot.context.properties.EnableConfigurationProperties.class
        )).isTrue();
    }
}
