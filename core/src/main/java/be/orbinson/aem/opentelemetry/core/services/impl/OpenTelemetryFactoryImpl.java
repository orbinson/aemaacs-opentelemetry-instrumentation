package be.orbinson.aem.opentelemetry.core.services.impl;

import aQute.bnd.annotation.Cardinality;
import aQute.bnd.annotation.Resolution;
import aQute.bnd.annotation.spi.ServiceConsumer;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.common.export.HttpSenderProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        immediate = true
)
@ServiceConsumer(
        value = HttpSenderProvider.class,
        resolution = Resolution.MANDATORY
)
@ServiceConsumer(
        value = ConfigurableSpanExporterProvider.class,
        resolution = Resolution.MANDATORY,
        cardinality = Cardinality.MULTIPLE
)
public class OpenTelemetryFactoryImpl implements OpenTelemetryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OpenTelemetryFactoryImpl.class);

    @Reference
    private OpenTelemetryConfig config;

    private OpenTelemetry openTelemetry;

    @Activate
    public void activate() {
        if (config.enabled()) {
            if (config.useGlobalOpenTelemetry()) {
                openTelemetry = GlobalOpenTelemetry.get();
                LOG.info("OpenTelemetry is enabled from Global: {}", openTelemetry);
            } else {
                openTelemetry = AutoConfiguredOpenTelemetrySdk.builder().build().getOpenTelemetrySdk();
                LOG.info("OpenTelemetry is enabled with SDK: {}", openTelemetry);
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        if (!config.useGlobalOpenTelemetry() && openTelemetry != null) {
            OpenTelemetrySdk sdk = (OpenTelemetrySdk) openTelemetry;
            sdk.close();
        }
    }

    @Override
    public OpenTelemetry get() {
        if (openTelemetry != null) {
            return openTelemetry;
        }
        return OpenTelemetry.noop();
    }
}
