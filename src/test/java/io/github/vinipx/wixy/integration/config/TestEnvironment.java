package io.github.vinipx.wixy.integration.config;

public final class TestEnvironment {

    public static final String BASE_URL_PROPERTY = "wixy.test.base-url";
    public static final String API_KEY_PROPERTY = "wixy.test.api-key";
    public static final String DEFAULT_TEST_API_KEY = "integration-test-key-99999";

    private TestEnvironment() {}

    public static boolean isRemote() {
        String url = System.getProperty(BASE_URL_PROPERTY, "");
        return !url.isBlank();
    }

    public static String getRemoteBaseUrl() {
        return System.getProperty(BASE_URL_PROPERTY, "").replaceAll("/+$", "");
    }

    public static String getApiKey() {
        String key = System.getProperty(API_KEY_PROPERTY, "");
        return (key != null && !key.isBlank()) ? key : DEFAULT_TEST_API_KEY;
    }

    public static String resolveBaseUrl(int localPort) {
        if (isRemote()) {
            return getRemoteBaseUrl();
        }
        return "http://localhost:" + localPort;
    }
}
