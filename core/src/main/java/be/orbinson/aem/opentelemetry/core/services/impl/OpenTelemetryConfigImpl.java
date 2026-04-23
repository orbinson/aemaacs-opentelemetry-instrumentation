package be.orbinson.aem.opentelemetry.core.services.impl;

import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component
@Designate(ocd = OpenTelemetryConfigImpl.Config.class)
public class OpenTelemetryConfigImpl implements OpenTelemetryConfig {

    @ObjectClassDefinition(name = "OpenTelemetry Configuration")
    @interface Config {
        @AttributeDefinition(description = "Enable telemetry")
        boolean enabled() default false;

        @AttributeDefinition(description = "Instrumentation scope name for spans")
        String instrumentationScopeName() default "aem";

        @AttributeDefinition(description = "Trace components as a separate span")
        boolean traceComponents() default false;

        @AttributeDefinition(description = "Use the global opentelemetry instead of creating one with the SDK")
        boolean useGlobalOpenTelemetry() default false;
    }

    private boolean enabled;
    private String instrumentationScopeName;
    private boolean traceComponents;
    private boolean useGlobalOpenTelemetry;

    @Activate
    protected void activate(Config config) {
        this.enabled = config.enabled();
        this.instrumentationScopeName = config.instrumentationScopeName();
        this.traceComponents = config.traceComponents();
        this.useGlobalOpenTelemetry = config.useGlobalOpenTelemetry();
    }

    @Override
    public boolean enabled() {
        return enabled;
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

}
