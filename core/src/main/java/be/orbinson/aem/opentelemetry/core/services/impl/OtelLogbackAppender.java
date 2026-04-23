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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Forwards SLF4J log events to the OpenTelemetry logs bridge by registering as a Logback
 * Appender service. Sling Commons Log's AppenderTracker discovers services with the
 * {@code loggers} property and attaches them to the named loggers in the Logback context.
 *
 * <p>Logback is declared as {@code DynamicImport-Package} so this component activates only
 * when logback classes are available. If logback becomes unavailable in a future AEM release
 * DS marks this component unsatisfied while all other bundle components keep working.
 */
@Component(immediate = true, service = Appender.class)
@Designate(ocd = OtelLogbackAppender.Config.class)
public class OtelLogbackAppender extends AppenderBase<ILoggingEvent> {

    @ObjectClassDefinition(name = "OpenTelemetry Logback Appender")
    @interface Config {
        // AppenderTracker filter: (&(objectClass=ch.qos.logback.core.Appender)(loggers=*))
        // "ROOT" attaches to the root logger, forwarding all SLF4J events.
        @AttributeDefinition(description = "Logback logger name to attach to. Use ROOT for all loggers.")
        String loggers() default "ROOT";
    }

    private static final Logger LOG = LoggerFactory.getLogger(OtelLogbackAppender.class);

    @Reference
    private OpenTelemetryFactory openTelemetryFactory;

    @Reference
    private OpenTelemetryConfig config;

    @Activate
    protected void activate(Config appenderConfig) {
        if (config.enabled() && config.enableLogBridge()) {
            LOG.info("OtelLogbackAppender activated: forwarding SLF4J logs to OpenTelemetry");
        }
    }

    @Deactivate
    protected void deactivate() {
        stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!config.enabled() || !config.enableLogBridge()) {
            return;
        }
        String loggerName = event.getLoggerName();
        if (!isAccepted(loggerName)) {
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

    private boolean isAccepted(String loggerName) {
        String[] names = config.loggerNames();
        if (names == null || names.length == 0) {
            return true;
        }
        if (loggerName == null) {
            return false;
        }
        for (String prefix : names) {
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
