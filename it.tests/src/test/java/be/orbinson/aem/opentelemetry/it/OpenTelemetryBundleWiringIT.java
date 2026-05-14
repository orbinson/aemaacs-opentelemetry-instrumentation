package be.orbinson.aem.opentelemetry.it;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Boots Sling Starter + our cp-converted feature via the feature-launcher Maven plugin
 * (pre-integration-test phase). Verifies:
 *
 * <ul>
 *   <li>api and core bundles reach ACTIVE</li>
 *   <li>Toggling {@code enableLogAppender=true} registers an {@code Appender} service with
 *       the configured {@code loggers} property</li>
 *   <li>Sling AppenderTracker attaches the appender to the named Logback loggers</li>
 * </ul>
 */
class OpenTelemetryBundleWiringIT {

    private static final int SLING_PORT = Integer.getInteger("sling.http.port", 8080);
    private static final String BASE_URL = "http://localhost:" + SLING_PORT;
    private static final String AUTH = "Basic "
            + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
    private static final String OTEL_PID =
            "be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl";
    private static final String CORE_BSN =
            "be.orbinson.aem.aemaacs-opentelemetry-instrumentation.core";
    private static final String API_BSN =
            "be.orbinson.aem.aemaacs-opentelemetry-instrumentation.api";

    private static CloseableHttpClient http;

    @BeforeAll
    static void setUp() {
        http = HttpClients.createDefault();
        // Sling Starter + cp-converted feature install + bundle activation. Allow generous
        // slack for CI cold-starts.
        await().atMost(240, SECONDS).pollInterval(2, SECONDS)
                .until(() -> "Active".equals(bundleState(API_BSN)));
        await().atMost(120, SECONDS).pollInterval(2, SECONDS)
                .until(() -> "Active".equals(bundleState(CORE_BSN)));
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (http != null) {
            http.close();
        }
    }

    @Test
    void enablingConfigRegistersAppenderWithLoggersProperty() throws Exception {
        applyConfig(true, true, new String[]{"ROOT"});

        await().atMost(30, SECONDS).pollInterval(1, SECONDS)
                .until(() -> findOurAppenderService() != null);

        JsonObject service = findOurAppenderService();
        assertNotNull(service);
        JsonArray loggers = findProp(service, "loggers");
        assertNotNull(loggers, "service should have a `loggers` property. Service was:\n" + service);
        assertEquals(1, loggers.size());
        assertEquals("ROOT", loggers.get(0).getAsString());
    }

    @Test
    void configuredLoggerNamesPropagateToServiceProperty() throws Exception {
        applyConfig(true, true, new String[]{"ROOT", "log.request", "log.access"});

        await().atMost(30, SECONDS).pollInterval(1, SECONDS).until(() -> {
            JsonObject s = findOurAppenderService();
            if (s == null) return false;
            JsonArray l = findProp(s, "loggers");
            return l != null && l.size() == 3;
        });

        JsonArray loggers = findProp(findOurAppenderService(), "loggers");
        List<String> values = new ArrayList<>();
        loggers.forEach(e -> values.add(e.getAsString()));
        assertTrue(values.contains("ROOT"));
        assertTrue(values.contains("log.request"));
        assertTrue(values.contains("log.access"));
    }

    @Test
    void disablingAppenderUnregistersService() throws Exception {
        applyConfig(true, true, new String[]{"ROOT"});
        await().atMost(30, SECONDS).pollInterval(1, SECONDS)
                .until(() -> findOurAppenderService() != null);

        applyConfig(true, false, new String[]{"ROOT"});

        await().atMost(30, SECONDS).pollInterval(1, SECONDS)
                .until(() -> findOurAppenderService() == null);
    }

    @Test
    void appenderAttachedToConfiguredLogbackLoggers() throws Exception {
        applyConfig(true, true, new String[]{"ROOT"});

        await().atMost(30, SECONDS).pollInterval(1, SECONDS).until(() -> {
            String html = httpGetText("/system/console/slinglog");
            return html.contains("OtelLogbackAppender");
        });

        String html = httpGetText("/system/console/slinglog");
        assertTrue(html.contains("OtelLogbackAppender"),
                "Sling log console should list our appender attached to ROOT logger");
    }

    private static void applyConfig(boolean enabled, boolean enableLogAppender, String[] loggers)
            throws IOException {
        HttpPost post = new HttpPost(BASE_URL + "/system/console/configMgr/" + OTEL_PID);
        post.addHeader("Authorization", AUTH);
        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("apply", "true"));
        params.add(new BasicNameValuePair("action", "ajaxConfigManager"));
        params.add(new BasicNameValuePair("propertylist",
                "enabled,enableLogAppender,instrumentationScopeName,traceComponents,"
                        + "useGlobalOpenTelemetry,loggerNames"));
        params.add(new BasicNameValuePair("enabled", String.valueOf(enabled)));
        params.add(new BasicNameValuePair("enableLogAppender", String.valueOf(enableLogAppender)));
        params.add(new BasicNameValuePair("instrumentationScopeName", "aem"));
        params.add(new BasicNameValuePair("traceComponents", "false"));
        params.add(new BasicNameValuePair("useGlobalOpenTelemetry", "false"));
        for (String l : loggers) {
            params.add(new BasicNameValuePair("loggerNames", l));
        }
        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        try (CloseableHttpResponse resp = http.execute(post)) {
            int code = resp.getStatusLine().getStatusCode();
            if (code != 200 && code != 302) {
                fail("Config apply failed with HTTP " + code);
            }
        }
    }

    private static String bundleState(String symbolicName) {
        try {
            JsonObject root = httpGetJson("/system/console/bundles.json");
            for (var b : root.getAsJsonArray("data")) {
                JsonObject bundle = b.getAsJsonObject();
                if (symbolicName.equals(bundle.get("symbolicName").getAsString())) {
                    return bundle.get("state").getAsString();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * The summary endpoint /system/console/services.json returns each service without its
     * properties. To get props (including our `loggers` array), fetch
     * /system/console/services/{id}.json for the matching service id.
     */
    private static JsonObject findOurAppenderService() {
        try {
            JsonObject root = httpGetJson("/system/console/services.json");
            for (var s : root.getAsJsonArray("data")) {
                JsonObject svc = s.getAsJsonObject();
                String types = svc.has("types") ? svc.get("types").getAsString() : "";
                String bsn = svc.has("bundleSymbolicName")
                        ? svc.get("bundleSymbolicName").getAsString() : "";
                if (types.contains("ch.qos.logback.core.Appender") && CORE_BSN.equals(bsn)) {
                    String id = svc.get("id").getAsString();
                    JsonObject detail = httpGetJson("/system/console/services/" + id + ".json");
                    // Detail endpoint returns the same shape: {data:[{...props:[...]...}]}
                    for (var d : detail.getAsJsonArray("data")) {
                        return d.getAsJsonObject();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Felix Webconsole serializes service properties in a few different shapes:
     *  - JsonArray  ["a","b"]   (newer versions)
     *  - String     "[a, b]"    (older versions render arrays as a bracketed CSV)
     *  - String     "a"         (single-value)
     * Handle all three.
     */
    private static JsonArray findProp(JsonObject service, String key) {
        if (!service.has("props")) return null;
        for (var p : service.getAsJsonArray("props")) {
            JsonObject prop = p.getAsJsonObject();
            if (!key.equals(prop.get("key").getAsString())) continue;
            var value = prop.get("value");
            if (value.isJsonArray()) {
                return value.getAsJsonArray();
            }
            String raw = value.getAsString();
            JsonArray arr = new JsonArray();
            if (raw.startsWith("[") && raw.endsWith("]")) {
                String body = raw.substring(1, raw.length() - 1).trim();
                if (!body.isEmpty()) {
                    for (String s : body.split("\\s*,\\s*")) {
                        arr.add(s);
                    }
                }
            } else {
                arr.add(raw);
            }
            return arr;
        }
        return null;
    }

    private static JsonObject httpGetJson(String path) throws IOException {
        return JsonParser.parseString(httpGetText(path)).getAsJsonObject();
    }

    private static String httpGetText(String path) throws IOException {
        HttpGet get = new HttpGet(BASE_URL + path);
        get.addHeader("Authorization", AUTH);
        try (CloseableHttpResponse resp = http.execute(get)) {
            return EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        }
    }
}
