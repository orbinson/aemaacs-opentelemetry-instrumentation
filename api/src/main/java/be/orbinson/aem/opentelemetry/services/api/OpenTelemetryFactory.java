package be.orbinson.aem.opentelemetry.services.api;

import io.opentelemetry.api.OpenTelemetry;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface OpenTelemetryFactory {
    OpenTelemetry get();
}
