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

        @AttributeDefinition(description = "Enable Logback appender to forward AEM logs to OpenTelemetry")
        boolean enableLogAppender() default false;

        @AttributeDefinition(description = "Instrumentation scope name for spans")
        String instrumentationScopeName() default "aem";

        @AttributeDefinition(description = "Trace components as a separate span")
        boolean traceComponents() default false;

        @AttributeDefinition(description = "Use the global opentelemetry instead of creating one with the SDK")
        boolean useGlobalOpenTelemetry() default false;

        @AttributeDefinition(description = "Logback loggers to attach the appender to")
        String[] loggerNames() default {"ROOT"};
    }

    private boolean enabled;
    private boolean enableLogAppender;
    private String instrumentationScopeName;
    private boolean traceComponents;
    private boolean useGlobalOpenTelemetry;
    private String[] loggerNames;

    @Activate
    protected void activate(Config config) {
        this.enabled = config.enabled();
        this.enableLogAppender = config.enableLogAppender();
        this.instrumentationScopeName = config.instrumentationScopeName();
        this.traceComponents = config.traceComponents();
        this.useGlobalOpenTelemetry = config.useGlobalOpenTelemetry();
        this.loggerNames = config.loggerNames();
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
    public boolean traceComponents() {
        return traceComponents;
    }

    @Override
    public boolean useGlobalOpenTelemetry() {
        return useGlobalOpenTelemetry;
    }

    @Override
    public String[] loggerNames() {
        return Arrays.copyOf(loggerNames, loggerNames.length);
    }

}
