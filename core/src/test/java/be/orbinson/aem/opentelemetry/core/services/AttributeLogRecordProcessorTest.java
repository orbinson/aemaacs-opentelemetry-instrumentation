package be.orbinson.aem.opentelemetry.core.services;

import be.orbinson.aem.opentelemetry.core.services.AttributeLogRecordProcessor;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class AttributeLogRecordProcessorTest {

    private final AttributeLogRecordProcessor processor = new AttributeLogRecordProcessor();

    @Mock
    private ReadWriteLogRecord readWriteLogRecord;

    @Mock
    private LogRecordData logRecordData;

    @Mock
    private InstrumentationScopeInfo instrumentationScopeInfo;

    @Test
    void testEmit() {
        doReturn(logRecordData).when(readWriteLogRecord).toLogRecordData();
        doReturn(instrumentationScopeInfo).when(logRecordData).getInstrumentationScopeInfo();
        doReturn("ROOT").when(instrumentationScopeInfo).getName();
        processor.onEmit(null, readWriteLogRecord);
        verify(readWriteLogRecord).setAttribute(AttributeKey.stringKey("host"), "localhost");
        verify(readWriteLogRecord).setAttribute(AttributeKey.stringKey("logger"), "ROOT");
    }
}
