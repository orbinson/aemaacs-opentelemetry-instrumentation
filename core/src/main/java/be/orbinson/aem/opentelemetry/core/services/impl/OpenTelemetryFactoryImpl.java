package be.orbinson.aem.opentelemetry.core.services.impl;

import aQute.bnd.annotation.Resolution;
import aQute.bnd.annotation.spi.ServiceConsumer;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.internal.http.HttpSenderProvider;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component(
        immediate = true
)
@ServiceConsumer(
        value = HttpSenderProvider.class,
        resolution = Resolution.MANDATORY
)
@ServiceConsumer(
        value = ConfigurableSpanExporterProvider.class,
        resolution = Resolution.MANDATORY
)
public class OpenTelemetryFactoryImpl implements OpenTelemetryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OpenTelemetryFactoryImpl.class);

    @Reference
    private OpenTelemetryConfig config;

    private OpenTelemetry openTelemetry;
    private OpenTelemetryAppender openTelemetryAppender;

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
            if (config.enableLogAppender()) {
                addOpenTelemetryAppenderToRoot();
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        if (!config.useGlobalOpenTelemetry() && openTelemetry != null) {
            detachOpenTelemetryAppenderFromRoot();
            OpenTelemetrySdk sdk = (OpenTelemetrySdk) openTelemetry;
            sdk.close();
        }
    }

    private void addOpenTelemetryAppenderToRoot() {
        openTelemetryAppender = new OpenTelemetryAppender();
        openTelemetryAppender.setOpenTelemetry(openTelemetry);
        openTelemetryAppender.start();
        for (String loggerName : config.loggerNames()) {
            Logger logger = LoggerFactory.getLogger(loggerName);
            addAppenderToLogbackLogger(logger);
        }
    }

    private void detachOpenTelemetryAppenderFromRoot() {
        if (openTelemetryAppender != null) {
            Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            detachAppenderToLogbackLogger(logger);
        }
    }

    // Logger and LoggerContext are marked as deprecated by Adobe SDK because it's
    // not support in AEMaaCS however it still works to configure the appender
    private void addAppenderToLogbackLogger(Logger logger) {
        try {
            Method addAppenderMethod = logger.getClass().getMethod("addAppender", Class.forName("ch.qos.logback.core.Appender"));
            addAppenderMethod.invoke(logger, openTelemetryAppender);
        } catch (NoSuchMethodException | ClassNotFoundException
                 | IllegalAccessException | InvocationTargetException e) {
            logger.warn("Could not add appender", e);
        }
    }

    // Logger and LoggerContext are marked as deprecated by Adobe SDK because it's
    // not support in AEMaaCS however it still works to configure the appender
    private void detachAppenderToLogbackLogger(Logger logger) {
        try {
            Method detachAppenderMethod = logger.getClass().getMethod("detachAppender", Class.forName("ch.qos.logback.core.Appender"));
            detachAppenderMethod.invoke(logger, openTelemetryAppender);
        } catch (NoSuchMethodException | ClassNotFoundException
                 | IllegalAccessException | InvocationTargetException e) {
            logger.warn("Could not detach appender", e);
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
