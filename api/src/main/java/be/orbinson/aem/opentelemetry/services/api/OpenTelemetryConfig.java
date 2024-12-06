package be.orbinson.aem.opentelemetry.services.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface OpenTelemetryConfig {
    boolean enabled();

    boolean enableLogAppender();

    String instrumentationScopeName();

    boolean useGlobalOpenTelemetry();

    String[] loggerNames();

    boolean traceComponents();

    boolean exportMetrics();
}
