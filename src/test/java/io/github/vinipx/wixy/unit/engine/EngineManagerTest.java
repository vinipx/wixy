package io.github.vinipx.wixy.unit.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.engine.LocalWireMockEngine;
import io.github.vinipx.wixy.engine.RemoteWireMockEngine;
import io.github.vinipx.wixy.engine.WireMockEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@Tag("unit")
@DisplayName("EngineManager")
class EngineManagerTest {

    private final WireMockServer localServer = mock(WireMockServer.class);
    private final EngineManager manager = new EngineManager(localServer);

    @Test
    @DisplayName("Initial state should be local engine")
    void initialState() {
        assertThat(manager.getActiveEngine()).isInstanceOf(LocalWireMockEngine.class);
        assertThat(manager.getActiveServerId()).isNull();
    }

    @Test
    @DisplayName("switchToRemote should update active engine")
    void switchToRemote() {
        UUID id = UUID.randomUUID();
        manager.switchToRemote(id, "http://remote-host:8080");
        
        assertThat(manager.getActiveEngine()).isInstanceOf(RemoteWireMockEngine.class);
        assertThat(manager.getActiveServerId()).isEqualTo(id);
        assertThat(manager.getActiveEngine().getPort()).isEqualTo(8080);
    }

    @Test
    @DisplayName("switchToLocal should revert to local engine")
    void switchToLocal() {
        manager.switchToRemote(UUID.randomUUID(), "http://remote-host:8080");
        manager.switchToLocal();
        
        assertThat(manager.getActiveEngine()).isInstanceOf(LocalWireMockEngine.class);
        assertThat(manager.getActiveServerId()).isNull();
    }

    @Test
    @DisplayName("switchToRemote should throw on invalid URL")
    void invalidUrl() {
        assertThatThrownBy(() -> manager.switchToRemote(UUID.randomUUID(), "not a url"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("setRequestOverride should temporarily change active engine")
    void requestOverride() {
        LocalWireMockEngine override = new LocalWireMockEngine(mock(WireMockServer.class));
        manager.setRequestOverride(override);
        
        assertThat(manager.getActiveEngine()).isEqualTo(override);
        
        manager.clearRequestOverride();
        assertThat(manager.getActiveEngine()).isNotEqualTo(override);
        assertThat(manager.getActiveEngine()).isInstanceOf(LocalWireMockEngine.class);
    }

    @Test
    @DisplayName("getEngineForUrl should create remote engine correctly")
    void getEngineForUrl() {
        WireMockEngine engine = manager.getEngineForUrl("https://secure-host:443");
        assertThat(engine).isInstanceOf(RemoteWireMockEngine.class);
        assertThat(engine.getPort()).isEqualTo(443);
    }
}
