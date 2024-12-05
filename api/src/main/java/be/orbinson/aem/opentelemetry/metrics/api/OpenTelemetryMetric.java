package be.orbinson.aem.opentelemetry.metrics.api;

import io.opentelemetry.api.metrics.Meter;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface OpenTelemetryMetric {
    void build(Meter meter);
}
