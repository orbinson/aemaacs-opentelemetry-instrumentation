package be.orbinson.aem.opentelemetry.core.services.impl;

import io.opentelemetry.api.OpenTelemetry;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class OpenTelemetryFactoryImplTest {

    private final SlingContext context = new SlingContext();

    @Spy
    private OpenTelemetryFactoryImpl openTelemetryFactory;

    // Disable exporting
    @BeforeAll
    static void beforeAll() {
        System.setProperty("otel.metrics.exporter", "none");
        System.setProperty("otel.traces.exporter", "none");
        System.setProperty("otel.logs.exporter", "none");
    }

    @Test
    void testActivateWhenConfigIsEnabled() {
        context.registerInjectActivateService(
                OpenTelemetryConfigImpl.class,
                "enabled", true
        );
        openTelemetryFactory = context.registerInjectActivateService(OpenTelemetryFactoryImpl.class);

        assertNotNull(openTelemetryFactory.get());
        assertNotEquals(OpenTelemetry.noop(), openTelemetryFactory.get());
    }

    @Test
    void testActivateAndDeactivate() {
        context.registerInjectActivateService(
                OpenTelemetryConfigImpl.class,
                "enabled", true
        );
        openTelemetryFactory = context.registerInjectActivateService(OpenTelemetryFactoryImpl.class);

        assertNotNull(openTelemetryFactory.get());
        openTelemetryFactory.deactivate();
    }

    @Test
    void testActivateWhenConfigIsDisabled() {
        context.registerInjectActivateService(OpenTelemetryConfigImpl.class);
        openTelemetryFactory = context.registerInjectActivateService(OpenTelemetryFactoryImpl.class);

        assertEquals(OpenTelemetry.noop(), openTelemetryFactory.get());
    }
}
