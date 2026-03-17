package be.orbinson.aem.opentelemetry.core.services.impl;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper class that isolates the logback appender dependency.
 * If logback is not available at runtime (e.g. removed from AEMaaCS),
 * this class will fail to load with a NoClassDefFoundError which callers
 * should catch to gracefully degrade.
 */
class LogbackAppenderHelper {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackAppenderHelper.class);

    private OpenTelemetryAppender openTelemetryAppender;

    void addAppender(OpenTelemetry openTelemetry, String[] loggerNames) {
        openTelemetryAppender = new OpenTelemetryAppender();
        openTelemetryAppender.setOpenTelemetry(openTelemetry);
        openTelemetryAppender.start();
        for (String loggerName : loggerNames) {
            Logger logger = LoggerFactory.getLogger(loggerName);
            addAppenderToLogbackLogger(logger);
        }
    }

    void detachAppender() {
        if (openTelemetryAppender != null) {
            Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            detachAppenderFromLogbackLogger(logger);
            openTelemetryAppender = null;
        }
    }

    // Logger and LoggerContext are marked as deprecated by Adobe SDK because it's
    // not supported in AEMaaCS however it still works to configure the appender
    private void addAppenderToLogbackLogger(Logger logger) {
        try {
            Method addAppenderMethod = logger.getClass().getMethod("addAppender", Class.forName("ch.qos.logback.core.Appender"));
            addAppenderMethod.invoke(logger, openTelemetryAppender);
        } catch (NoSuchMethodException | ClassNotFoundException
                 | IllegalAccessException | InvocationTargetException e) {
            LOG.warn("Could not add appender", e);
        }
    }

    // Logger and LoggerContext are marked as deprecated by Adobe SDK because it's
    // not supported in AEMaaCS however it still works to configure the appender
    private void detachAppenderFromLogbackLogger(Logger logger) {
        try {
            Method detachAppenderMethod = logger.getClass().getMethod("detachAppender", Class.forName("ch.qos.logback.core.Appender"));
            detachAppenderMethod.invoke(logger, openTelemetryAppender);
        } catch (NoSuchMethodException | ClassNotFoundException
                 | IllegalAccessException | InvocationTargetException e) {
            LOG.warn("Could not detach appender", e);
        }
    }
}
