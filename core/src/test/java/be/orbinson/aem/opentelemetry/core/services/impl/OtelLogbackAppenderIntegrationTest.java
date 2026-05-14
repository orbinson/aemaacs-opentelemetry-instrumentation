package be.orbinson.aem.opentelemetry.core.services.impl;

import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * End-to-end test that wires the appender to a real OpenTelemetry SDK with an
 * {@link InMemoryLogRecordExporter} and verifies emitted records flow through correctly.
 * Mirrors what the docker collector validates manually, but as a fast unit-test.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class OtelLogbackAppenderIntegrationTest {

    private final AemContext context = new AemContext();

    @Mock
    private OpenTelemetryConfig config;

    private InMemoryLogRecordExporter exporter;
    private OpenTelemetrySdk sdk;
    private OpenTelemetryFactory factory;

    @BeforeEach
    void setUp() {
        exporter = InMemoryLogRecordExporter.create();
        sdk = OpenTelemetrySdk.builder()
                .setLoggerProvider(
                        SdkLoggerProvider.builder()
                                .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
                                .build())
                .build();
        factory = () -> sdk;

        doReturn(true).when(config).enabled();
        doReturn(true).when(config).enableLogAppender();
        doReturn(new String[]{"ROOT"}).when(config).loggerNames();

        context.registerService(OpenTelemetryConfig.class, config);
        context.registerService(OpenTelemetryFactory.class, factory);
    }

    @AfterEach
    void tearDown() {
        if (sdk != null) {
            sdk.close();
        }
    }

    @Test
    void logEventFlowsThroughToExporter() {
        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        LoggingEvent event = newEvent(Level.ERROR, "com.example.Service", "boom");

        appender.append(event);

        List<LogRecordData> records = exporter.getFinishedLogRecordItems();
        assertEquals(1, records.size());
        LogRecordData record = records.get(0);
        assertEquals("boom", record.getBodyValue().asString());
        assertEquals("ERROR", record.getSeverityText());
        assertEquals("com.example.Service", record.getInstrumentationScopeInfo().getName());
    }

    @Test
    void exceptionAttributesAreSet() {
        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        LoggingEvent event = newEvent(Level.ERROR, "com.example.Service", "failure");
        event.setThrowableProxy(
                new ch.qos.logback.classic.spi.ThrowableProxy(new IllegalStateException("bad state")));

        appender.append(event);

        LogRecordData record = exporter.getFinishedLogRecordItems().get(0);
        assertEquals("java.lang.IllegalStateException",
                record.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey("exception.type")));
        assertEquals("bad state",
                record.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey("exception.message")));
        assertNotNull(record.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey("exception.stacktrace")));
    }

    @Test
    void reEntrantLogsAreSuppressed() {
        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);

        appender.append(newEvent(Level.ERROR, "io.opentelemetry.exporter.GrpcExporter", "export failed"));
        appender.append(newEvent(Level.WARN, "be.orbinson.aem.opentelemetry.core.Foo", "internal"));
        appender.append(newEvent(Level.INFO, "com.example.RealService", "real log"));

        List<LogRecordData> records = exporter.getFinishedLogRecordItems();
        assertEquals(1, records.size());
        assertEquals("real log", records.get(0).getBodyValue().asString());
    }

    @Test
    void severityLevelsMapCorrectly() {
        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);

        appender.append(newEvent(Level.ERROR, "com.example", "err"));
        appender.append(newEvent(Level.WARN, "com.example", "warn"));
        appender.append(newEvent(Level.INFO, "com.example", "info"));
        appender.append(newEvent(Level.DEBUG, "com.example", "debug"));
        appender.append(newEvent(Level.TRACE, "com.example", "trace"));

        List<LogRecordData> records = exporter.getFinishedLogRecordItems();
        assertEquals(5, records.size());
        assertEquals("ERROR", records.get(0).getSeverityText());
        assertEquals("WARN", records.get(1).getSeverityText());
        assertEquals("INFO", records.get(2).getSeverityText());
        assertEquals("DEBUG", records.get(3).getSeverityText());
        assertEquals("TRACE", records.get(4).getSeverityText());
    }

    @Test
    void timestampIsPreserved() {
        OtelLogbackAppender appender = context.registerInjectActivateService(OtelLogbackAppender.class);
        long ts = System.currentTimeMillis();
        LoggingEvent event = newEvent(Level.INFO, "com.example", "msg");
        event.setTimeStamp(ts);

        appender.append(event);

        LogRecordData record = exporter.getFinishedLogRecordItems().get(0);
        long recordedMillis = record.getTimestampEpochNanos() / 1_000_000;
        assertTrue(Math.abs(recordedMillis - ts) < 10, "timestamp should match within rounding");
    }

    private static LoggingEvent newEvent(Level level, String loggerName, String message) {
        LoggingEvent event = new LoggingEvent();
        event.setLevel(level);
        event.setLoggerName(loggerName);
        event.setMessage(message);
        event.setTimeStamp(System.currentTimeMillis());
        return event;
    }
}
