package be.orbinson.aem.opentelemetry.services.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface OpenTelemetryConfig {
    boolean enabled();

    boolean enableLogBridge();

    String instrumentationScopeName();

    boolean useGlobalOpenTelemetry();

    boolean traceComponents();

    String[] loggerNames();
}
