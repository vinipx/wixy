package io.github.vinipx.wixy.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Externalised configuration for Wixy, bound from {@code wixy.*} properties.
 */
@ConfigurationProperties(prefix = "wixy")
@Validated
public class WixyProperties {

    @Valid
    @NotNull
    private Wiremock wiremock = new Wiremock();

    @Valid
    @NotNull
    private Proxy proxy = new Proxy();

    @Valid
    @NotNull
    private Security security = new Security();

    @Valid
    @NotNull
    private Registry registry = new Registry();

    @Valid
    @NotNull
    private UI ui = new UI();

    // ── Getters / Setters ───────────────────────────────────────

    public Wiremock getWiremock() {
        return wiremock;
    }

    public void setWiremock(Wiremock wiremock) {
        this.wiremock = wiremock;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public UI getUi() {
        return ui;
    }

    public void setUi(UI ui) {
        this.ui = ui;
    }

    // ── Nested classes ──────────────────────────────────────────

    public static class Wiremock {

        @Min(0)
        @Max(65535)
        private int port = 9090;

        private boolean verbose = true;

        private String rootDir = "classpath:/wiremock";

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isVerbose() {
            return verbose;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public String getRootDir() {
            return rootDir;
        }

        public void setRootDir(String rootDir) {
            this.rootDir = rootDir;
        }
    }

    public static class Proxy {

        private boolean enabled = false;

        private String targetUrl = "";

        private boolean record = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTargetUrl() {
            return targetUrl;
        }

        public void setTargetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
        }

        public boolean isRecord() {
            return record;
        }

        public void setRecord(boolean record) {
            this.record = record;
        }
    }

    public static class Security {

        private boolean enabled = false;

        private String apiKey = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Registry {

        private String persistence = "file";

        private String filePath = System.getProperty("user.home") + "/.wixy/servers.json";

        public String getPersistence() {
            return persistence;
        }

        public void setPersistence(String persistence) {
            this.persistence = persistence;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }

    public static class UI {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
