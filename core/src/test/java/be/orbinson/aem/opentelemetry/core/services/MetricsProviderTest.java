package be.orbinson.aem.opentelemetry.core.services;

import be.orbinson.aem.mocks.MockDistributionAgent;
import be.orbinson.aem.opentelemetry.core.metrics.DistributionQueueGauage;
import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl;
import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryFactoryImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class MetricsProviderTest {

    private final AemContext context = new AemContext();

    @Spy
    private MetricsProvider metricsProvider;

    private DistributionQueueGauage distributionQueueGauage;

    @Test
    void testMetricsProviderBindsToService() {
        context.registerInjectActivateService(
                OpenTelemetryConfigImpl.class,
                "enabled", true
        );
        context.registerInjectActivateService(
                OpenTelemetryFactoryImpl.class,
                "enabled", true
        );
        context.registerInjectActivateService(MockDistributionAgent.class);
        distributionQueueGauage = context.registerInjectActivateService(DistributionQueueGauage.class);
        metricsProvider = context.registerInjectActivateService(MetricsProvider.class);

        assertNotNull(metricsProvider);
        assertEquals(1, metricsProvider.getMetrics().size());
    }

}
