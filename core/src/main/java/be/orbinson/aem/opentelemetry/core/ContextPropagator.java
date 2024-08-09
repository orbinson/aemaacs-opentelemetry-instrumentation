package be.orbinson.aem.opentelemetry.core;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class ContextPropagator {
    private static final Logger log = LoggerFactory.getLogger(ContextPropagator.class);

    private ContextPropagator() {
    }

    public static final TextMapGetter<SlingHttpServletRequest> getter =
            new TextMapGetter<>() {
                @Override
                public String get(SlingHttpServletRequest request, String key) {
                    log.trace("Reading propagated context from request '{}' with key '{}'", request, key);
                    return request != null ? request.getHeader(key) : null;
                }

                @Override
                public Iterable<String> keys(SlingHttpServletRequest request) {
                    return Collections.list(request.getHeaderNames());
                }
            };

    public static final TextMapSetter<SlingHttpServletResponse> setter =
            (response, key, value) -> {
                log.trace("Set propagated context to response '{}' with '{}={}'", response, key, value);
                if (response != null) {
                    response.setHeader(key, value);
                }
            };
}
