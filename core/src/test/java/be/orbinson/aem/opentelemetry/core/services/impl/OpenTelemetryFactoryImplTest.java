package be.orbinson.aem.opentelemetry.core.services.impl;

import io.opentelemetry.api.OpenTelemetry;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class OpenTelemetryFactoryImplTest {

    private final AemContext context = new AemContext();

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
                "enabled", true,
                "enableLogAppender", true
        );
        openTelemetryFactory = context.registerInjectActivateService(OpenTelemetryFactoryImpl.class);

        assertNotNull(openTelemetryFactory.get());
    }

    @Test
    void testActivateWhenConfigIsDisabled() {
        context.registerInjectActivateService(OpenTelemetryConfigImpl.class);
        openTelemetryFactory = context.registerInjectActivateService(OpenTelemetryFactoryImpl.class);

        assertEquals(OpenTelemetry.noop(), openTelemetryFactory.get());
    }
}
