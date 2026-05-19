package be.orbinson.aem.opentelemetry.core.services.impl;

import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

/**
 * Forwards SLF4J log events to the OpenTelemetry logs bridge. Registers itself programmatically
 * as a {@link Appender} service with a {@code loggers} property derived from
 * {@link OpenTelemetryConfig#loggerNames()}. Sling Commons Log's AppenderTracker discovers
 * services with that property and attaches them to the named Logback loggers.
 *
 * <p>Logback is declared as {@code DynamicImport-Package} so this component activates only
 * when logback classes are available. If logback ever becomes unavailable in a future AEM
 * release DS will leave this component unsatisfied while every other component keeps working.
 */
@Component(immediate = true)
public class OtelLogbackAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(OtelLogbackAppender.class);

    // Logger name prefixes whose events must never be forwarded — feeding the OTel SDK's own
    // log output back into itself causes recursive emission once the exporter starts failing.
    private static final String[] RE_ENTRANCY_PREFIXES = {
        "io.opentelemetry.",
        "be.orbinson.aem.opentelemetry."
    };

    @Reference
    private OpenTelemetryFactory openTelemetryFactory;

    @Reference
    private OpenTelemetryConfig config;

    private ServiceRegistration<Appender> registration;

    @Activate
    protected void activate(BundleContext context) {
        if (!config.enabled() || !config.enableLogAppender()) {
            return;
        }
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("loggers", config.loggerNames());
        registration = context.registerService(Appender.class, this, props);
        LOG.info("OtelLogbackAppender activated: forwarding SLF4J logs to OpenTelemetry");
    }

    @Deactivate
    protected void deactivate() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        String loggerName = event.getLoggerName();
        if (isReEntrant(loggerName)) {
            return;
        }
        String resolvedName = loggerName != null ? loggerName : "unknown";
        LogRecordBuilder builder = openTelemetryFactory.get()
                .getLogsBridge()
                .loggerBuilder(resolvedName)
                .build()
                .logRecordBuilder()
                .setTimestamp(event.getTimeStamp(), TimeUnit.MILLISECONDS)
                .setSeverity(toSeverity(event.getLevel()))
                .setSeverityText(event.getLevel().levelStr)
                .setBody(event.getFormattedMessage());

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            builder.setAttribute(AttributeKey.stringKey("exception.type"), throwableProxy.getClassName());
            if (throwableProxy.getMessage() != null) {
                builder.setAttribute(AttributeKey.stringKey("exception.message"), throwableProxy.getMessage());
            }
            builder.setAttribute(AttributeKey.stringKey("exception.stacktrace"), ThrowableProxyUtil.asString(throwableProxy));
        }

        builder.emit();
    }

    private static boolean isReEntrant(String loggerName) {
        if (loggerName == null) {
            return false;
        }
        for (String prefix : RE_ENTRANCY_PREFIXES) {
            if (loggerName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static Severity toSeverity(Level level) {
        if (level == null) {
            return Severity.UNDEFINED_SEVERITY_NUMBER;
        }
        switch (level.toInt()) {
            case Level.ERROR_INT: return Severity.ERROR;
            case Level.WARN_INT: return Severity.WARN;
            case Level.INFO_INT: return Severity.INFO;
            case Level.DEBUG_INT: return Severity.DEBUG;
            case Level.TRACE_INT: return Severity.TRACE;
            default: return Severity.UNDEFINED_SEVERITY_NUMBER;
        }
    }
}
