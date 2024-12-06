package be.orbinson.aem.opentelemetry.core.services.impl;

import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.Arrays;

@Component
@Designate(ocd = OpenTelemetryConfigImpl.Config.class)
public class OpenTelemetryConfigImpl implements OpenTelemetryConfig {
    
    @ObjectClassDefinition(name = "OpenTelemetry Configuration")
    @interface Config {
        @AttributeDefinition(description = "Enable telemetry")
        boolean enabled() default false;

        @AttributeDefinition(description = "Enable logging appender to bridge logs")
        boolean enableLogAppender() default false;

        @AttributeDefinition(description = "Instrumentation scope name for spans")
        String instrumentationScopeName() default "aem";

        @AttributeDefinition(description = "Logger names to append")
        String[] loggerNames() default {"ROOT"};

        @AttributeDefinition(description = "Trace components as a separate span")
        boolean traceComponents() default false;
        
        @AttributeDefinition(description = "Export metrics")
        boolean exportMetrics() default false;

        @AttributeDefinition(description = "Use the global opentelemetry instead of creating one with the SDK")
        boolean useGlobalOpenTelemetry() default false;
    }

    private boolean enabled;
    private boolean enableLogAppender;
    private String instrumentationScopeName;
    private String[] loggerNames;
    private boolean traceComponents;
    private boolean exportMetrics;
    private boolean useGlobalOpenTelemetry;

    @Activate
    protected void activate(Config config) {
        this.enabled = config.enabled();
        this.enableLogAppender = config.enableLogAppender();
        this.instrumentationScopeName = config.instrumentationScopeName();
        this.loggerNames = config.loggerNames();
        this.traceComponents = config.traceComponents();
        this.exportMetrics = config.exportMetrics();
        this.useGlobalOpenTelemetry = config.useGlobalOpenTelemetry();
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public boolean enableLogAppender() {
        return enableLogAppender;
    }

    @Override
    public String instrumentationScopeName() {
        return instrumentationScopeName;
    }

    @Override
    public String[] loggerNames() {
        return Arrays.copyOf(loggerNames, loggerNames.length);
    }

    @Override
    public boolean traceComponents() {
        return traceComponents;
    }

    @Override
    public boolean exportMetrics() {
        return exportMetrics;
    }

    @Override
    public boolean useGlobalOpenTelemetry() {
        return useGlobalOpenTelemetry;
    }

}
