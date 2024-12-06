package be.orbinson.aem.util;

import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl;
import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryFactoryImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class OpenTelemetryTest {

    public final AemContext context = new AemContext();

    public OpenTelemetryFactoryImpl openTelemetryFactory;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("otel.metrics.exporter", "none");
        System.setProperty("otel.traces.exporter", "none");
        System.setProperty("otel.logs.exporter", "none");
    }

    public void registerInjectActivateOpenTelemetryService(Object... config) {
        context.registerInjectActivateService(OpenTelemetryConfigImpl.class, config);
        openTelemetryFactory = context.registerInjectActivateService(OpenTelemetryFactoryImpl.class);
    }
}
