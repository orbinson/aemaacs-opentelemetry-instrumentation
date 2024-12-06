package be.orbinson.aem.opentelemetry.core.services.impl;

import be.orbinson.aem.util.OpenTelemetryTest;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenTelemetryFactoryImplTest extends OpenTelemetryTest {

    @Test
    void testActivateWhenConfigIsEnabled() {
        registerInjectActivateOpenTelemetryService(
                "enabled", true,
                "enableLogAppender", true
        );

        assertNotNull(openTelemetryFactory.get());
    }

    @Test
    void testActivateWhenConfigIsDisabled() {
        registerInjectActivateOpenTelemetryService();
        openTelemetryFactory = context.registerInjectActivateService(OpenTelemetryFactoryImpl.class);

        assertEquals(OpenTelemetry.noop(), openTelemetryFactory.get());
    }
}
