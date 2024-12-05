package be.orbinson.aem.opentelemetry.core.services;

import be.orbinson.aem.opentelemetry.metrics.api.OpenTelemetryMetric;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component
public class MetricsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsProvider.class);
    private final List<OpenTelemetryMetric> metrics = new ArrayList<>();

    @Reference
    private OpenTelemetryFactory openTelemetryFactory;

    @Reference
    private OpenTelemetryConfig openTelemetryConfig;

    @Reference(
            bind = "addMetric",
            unbind = "removeMetric"
    )
    void addMetric(OpenTelemetryMetric metric) {
        LOG.debug("Add metric: {}", metric);
        metrics.add(metric);
    }

    void removeMetric(OpenTelemetryMetric metric) {
        LOG.debug("Remove metric: {}", metric);
        metrics.remove(metric);
    }

    @Activate
    protected void activate() {
        OpenTelemetry openTelemetry = openTelemetryFactory.get();
        Meter meter = openTelemetry.getMeter(openTelemetryConfig.instrumentationScopeName());

        for (OpenTelemetryMetric metric : metrics) {
            LOG.debug("Build metric: {}", metric);
            metric.build(meter);
        }
    }
}
