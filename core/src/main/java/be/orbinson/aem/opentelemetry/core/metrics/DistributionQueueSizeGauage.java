package be.orbinson.aem.opentelemetry.core.metrics;

import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import org.apache.sling.distribution.agent.spi.DistributionAgent;
import org.apache.sling.distribution.queue.spi.DistributionQueue;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DistributionQueueSizeGauage {

    private static final Logger LOG = LoggerFactory.getLogger(DistributionQueueSizeGauage.class);

    @Reference
    OpenTelemetryFactory openTelemetryFactory;

    @Reference
    OpenTelemetryConfig openTelemetryConfig;

    @Reference
    DistributionAgent distributionAgent;

    @Activate
    public void activate() {
        if (openTelemetryConfig.exportMetrics()) {
            OpenTelemetry openTelemetry = openTelemetryFactory.get();

            openTelemetry.getMeter(openTelemetryConfig.instrumentationScopeName())
                    .gaugeBuilder("distribution.queue.size")
                    .setDescription("The size of the distribution queue")
                    .setUnit("items")
                    .buildWithCallback(this::callback);
        }
    }

    public void callback(ObservableDoubleMeasurement measurement) {
        int size = 0;
        if (distributionAgent != null) {
            DistributionQueue distributionQueue = distributionAgent.getQueue("main");
            if (distributionQueue != null) {
                size = distributionQueue.getStatus().getItemsCount();
            } else {
                LOG.warn("Distribution queue 'main' is not available");
            }
        } else {
            LOG.warn("Distribution agent is not available");
        }
        measurement.record(size);
    }
}
