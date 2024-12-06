package be.orbinson.aem.opentelemetry.core.context;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(AemContextExtension.class)
class ContextPropagatorTest {
    public static final String TRACEPARENT_HEADER = "traceparent";
    public static final String TRACEPARENT_VALUE = "00-80e1afed08e019fc1110464cfa66635c-7a085853722dc6d2-01";
    private final AemContext context = new AemContext();

    @Test
    void traceParentIsSet() {
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        request.setHeader(TRACEPARENT_HEADER, TRACEPARENT_VALUE);

        assertEquals(TRACEPARENT_VALUE, ContextPropagator.getter.get(request, TRACEPARENT_HEADER));
    }

    @Test
    void traceParentIsNotSet() {
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());

        assertNull(ContextPropagator.getter.get(request, TRACEPARENT_HEADER));
    }

    @Test
    void requestIsNull() {
        assertNull(ContextPropagator.getter.get(null, TRACEPARENT_HEADER));
    }

    @Test
    void allHeadersAreReturned() {
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        request.setHeader(TRACEPARENT_HEADER, TRACEPARENT_VALUE);
        request.setHeader("origin", "localhost");

        assertEquals(ContextPropagator.getter.keys(request), List.of(TRACEPARENT_HEADER, "origin"));
    }

    @Test
    void setHeader() {
        MockSlingHttpServletResponse response = new MockSlingHttpServletResponse();

        ContextPropagator.setter.set(response, TRACEPARENT_HEADER, TRACEPARENT_VALUE);

        assertEquals(TRACEPARENT_VALUE, response.getHeader(TRACEPARENT_HEADER));
    }

    @Test
    void noHeaderIsSetWhenResponseIsNull() {
        assertDoesNotThrow(() -> ContextPropagator.setter.set(null, TRACEPARENT_HEADER, TRACEPARENT_VALUE));
    }

}
