package be.orbinson.aem.opentelemetry.core.services;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;

public class AttributeLogRecordProcessor implements LogRecordProcessor {

    @Override
    public void onEmit(Context context, ReadWriteLogRecord readWriteLogRecord) {
        String env = System.getenv("HOSTNAME");
        String hostname = env != null && !env.trim().isEmpty() ? env : "localhost";
        readWriteLogRecord.setAttribute(AttributeKey.stringKey("host"), hostname);
        readWriteLogRecord.setAttribute(
                AttributeKey.stringKey("logger"),
                readWriteLogRecord.toLogRecordData().getInstrumentationScopeInfo().getName()
        );
    }
}
