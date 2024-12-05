package be.orbinson.aem.opentelemetry.core.metrics;

import be.orbinson.aem.opentelemetry.metrics.api.OpenTelemetryMetric;
import io.opentelemetry.api.metrics.Meter;
import org.apache.sling.distribution.agent.spi.DistributionAgent;
import org.apache.sling.distribution.queue.spi.DistributionQueue;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = OpenTelemetryMetric.class)
public class DistributionQueueGauage implements OpenTelemetryMetric {

    private static final Logger LOG = LoggerFactory.getLogger(DistributionQueueGauage.class);
            
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    DistributionAgent distributionAgent;
    
    @Override
    public void build(Meter meter) {
        meter.gaugeBuilder("distribution.queue.size")
                .setDescription("The size of the distribution queue")
                .setUnit("items")
                .buildWithCallback(measurement -> {
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
                });
    }
}
