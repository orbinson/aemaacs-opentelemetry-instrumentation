package be.orbinson.aem.opentelemetry.core.metrics;

import be.orbinson.aem.mocks.MockDistributionAgent;
import be.orbinson.aem.util.OpenTelemetryTest;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

class DistributionQueueSizeGauageTest extends OpenTelemetryTest {
    @Spy
    ObservableDoubleMeasurement observableDoubleMeasurement;
    
    @Test
    void testActivateWhenConfigIsEnabled() {
        registerInjectActivateOpenTelemetryService(
                "enabled", true,
                "exportMetrics", true
        );
        context.registerInjectActivateService(MockDistributionAgent.class);
        DistributionQueueSizeGauage distributionQueueSizeGauage = context.registerInjectActivateService(DistributionQueueSizeGauage.class);
        
        distributionQueueSizeGauage.callback(observableDoubleMeasurement);
        
        assertNotNull(distributionQueueSizeGauage);
        verify(observableDoubleMeasurement).record(1.0);
    }
}
