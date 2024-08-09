package be.orbinson.aem.opentelemetry.core.services;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import org.apache.commons.lang3.StringUtils;

public class AttributeLogRecordProcessor implements LogRecordProcessor {

    @Override
    public void onEmit(Context context, ReadWriteLogRecord readWriteLogRecord) {
        String hostname = StringUtils.defaultIfBlank(System.getenv("HOSTNAME"), "localhost");
        readWriteLogRecord.setAttribute(AttributeKey.stringKey("host"), hostname);
        readWriteLogRecord.setAttribute(
                AttributeKey.stringKey("logger"),
                readWriteLogRecord.toLogRecordData().getInstrumentationScopeInfo().getName()
        );
    }
}
