package be.orbinson.aem.opentelemetry.core.services.impl;

import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import io.opentelemetry.api.OpenTelemetry;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

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
    void activateWhenEnabledDoesNotThrow() {
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogBridge();

        context.registerInjectActivateService(OtelLogbackAppender.class);
    }

    @Test
    void activateWhenDisabledDoesNotThrow() {
        doReturn(false).when(config).enabled();

        context.registerInjectActivateService(OtelLogbackAppender.class);
    }

    @Test
    void appendEmitsRecordWhenEnabled() {
        stubNoop();
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogBridge();
        doReturn(new String[]{}).when(config).loggerNames();
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
        doReturn(true).when(config).enableLogBridge();
        doReturn(new String[]{}).when(config).loggerNames();
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
    void appendWithMatchingPrefixIsForwarded() {
        stubNoop();
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogBridge();
        doReturn(new String[]{"be.orbinson"}).when(config).loggerNames();
        doReturn(Level.INFO).when(logEvent).getLevel();
        doReturn("be.orbinson.aem.MyService").when(logEvent).getLoggerName();
        doReturn("Info message").when(logEvent).getFormattedMessage();
        doReturn(System.currentTimeMillis()).when(logEvent).getTimeStamp();
        doReturn(null).when(logEvent).getThrowableProxy();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verify(logEvent).getFormattedMessage();
    }

    @Test
    void appendWithNonMatchingPrefixIsFiltered() {
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogBridge();
        doReturn(new String[]{"be.orbinson"}).when(config).loggerNames();
        doReturn("com.adobe.SomeClass").when(logEvent).getLoggerName();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verify(logEvent, never()).getFormattedMessage();
    }

    @Test
    void appendWhenBridgeDisabledIsNoop() {
        doReturn(true).when(config).enabled();
        doReturn(false).when(config).enableLogBridge();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verifyNoInteractions(logEvent);
    }

    @Test
    void appendWhenGloballyDisabledIsNoop() {
        doReturn(false).when(config).enabled();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.append(logEvent);

        verifyNoInteractions(logEvent);
    }

    @Test
    void appendWithWarnLevelMapsCorrectly() {
        stubNoop();
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogBridge();
        doReturn(new String[]{}).when(config).loggerNames();
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
    void deactivateDoesNotThrow() {
        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogBridge();

        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        appender.deactivate();
    }
}
