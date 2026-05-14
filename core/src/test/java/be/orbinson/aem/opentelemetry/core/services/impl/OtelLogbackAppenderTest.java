package be.orbinson.aem.opentelemetry.core.services.impl;

import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.Appender;
import io.opentelemetry.api.OpenTelemetry;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.ServiceReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class OtelLogbackAppenderTest {

    private final AemContext context = new AemContext();

    @Mock
    private OpenTelemetryConfig config;

    @Mock
    private OpenTelemetryFactory openTelemetryFactory;

    @Mock
    private ILoggingEvent logEvent;

    @BeforeEach
    void setUp() {
        context.registerService(OpenTelemetryConfig.class, config);
        context.registerService(OpenTelemetryFactory.class, openTelemetryFactory);
    }

    private void stubNoop() {
        doReturn(OpenTelemetry.noop()).when(openTelemetryFactory).get();
    }

    @Test
    void registersAppenderServiceWhenEnabled() {
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT"}).when(config).loggerNames();

        context.registerInjectActivateService(OtelLogbackAppender.class);

        ServiceReference<Appender> ref = context.bundleContext().getServiceReference(Appender.class);
        assertNotNull(ref, "Appender service should be registered");
        assertArrayEquals(new String[]{"ROOT"}, (String[]) ref.getProperty("loggers"));
    }

    @Test
    void registersAppenderWithConfiguredLoggers() {
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT", "log.request", "log.access"}).when(config).loggerNames();

        context.registerInjectActivateService(OtelLogbackAppender.class);

        ServiceReference<Appender> ref = context.bundleContext().getServiceReference(Appender.class);
        assertNotNull(ref);
        assertArrayEquals(new String[]{"ROOT", "log.request", "log.access"},
                (String[]) ref.getProperty("loggers"));
    }

    @Test
    void doesNotRegisterWhenAppenderDisabled() {
        doReturn(true).when(config).enabled();
        doReturn(false).when(config).enableLogAppender();

        context.registerInjectActivateService(OtelLogbackAppender.class);

        assertNull(context.bundleContext().getServiceReference(Appender.class));
    }

    @Test
    void doesNotRegisterWhenGloballyDisabled() {
        doReturn(false).when(config).enabled();

        context.registerInjectActivateService(OtelLogbackAppender.class);

        assertNull(context.bundleContext().getServiceReference(Appender.class));
    }

    @Test
    void appendEmitsRecord() {
        stubNoop();
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT"}).when(config).loggerNames();
        doReturn(Level.ERROR).when(logEvent).getLevel();
        doReturn("be.orbinson.Test").when(logEvent).getLoggerName();
        doReturn("Something went wrong").when(logEvent).getFormattedMessage();
        doReturn(System.currentTimeMillis()).when(logEvent).getTimeStamp();
        doReturn(null).when(logEvent).getThrowableProxy();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verify(logEvent).getFormattedMessage();
    }

    @Test
    void appendWithExceptionSetsAttributes() {
        stubNoop();
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT"}).when(config).loggerNames();
        doReturn(Level.ERROR).when(logEvent).getLevel();
        doReturn("be.orbinson.Test").when(logEvent).getLoggerName();
        doReturn("Error occurred").when(logEvent).getFormattedMessage();
        doReturn(System.currentTimeMillis()).when(logEvent).getTimeStamp();

        IThrowableProxy proxy = mock(IThrowableProxy.class);
        doReturn("java.lang.RuntimeException").when(proxy).getClassName();
        doReturn("test error").when(proxy).getMessage();
        doReturn(new ch.qos.logback.classic.spi.StackTraceElementProxy[0]).when(proxy).getStackTraceElementProxyArray();
        doReturn(proxy).when(logEvent).getThrowableProxy();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verify(logEvent, atLeastOnce()).getThrowableProxy();
    }

    @Test
    void appendSkipsReEntrantOpenTelemetryLogs() {
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT"}).when(config).loggerNames();
        doReturn("io.opentelemetry.exporter.internal.grpc.GrpcExporter").when(logEvent).getLoggerName();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verify(logEvent, never()).getFormattedMessage();
    }

    @Test
    void appendSkipsReEntrantOwnBundleLogs() {
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT"}).when(config).loggerNames();
        doReturn("be.orbinson.aem.opentelemetry.core.services.impl.OtelLogbackAppender")
                .when(logEvent).getLoggerName();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verify(logEvent, never()).getFormattedMessage();
    }

    @Test
    void appendWithWarnLevelMapsCorrectly() {
        stubNoop();
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT"}).when(config).loggerNames();
        doReturn(Level.WARN).when(logEvent).getLevel();
        doReturn("be.orbinson.Test").when(logEvent).getLoggerName();
        doReturn("Warning message").when(logEvent).getFormattedMessage();
        doReturn(System.currentTimeMillis()).when(logEvent).getTimeStamp();
        doReturn(null).when(logEvent).getThrowableProxy();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verify(logEvent, atLeastOnce()).getLevel();
    }

    @Test
    void deactivateUnregistersService() {
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT"}).when(config).loggerNames();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        assertNotNull(context.bundleContext().getServiceReference(Appender.class));

        appender.deactivate();
        verifyNoInteractions(logEvent);
    }
}
