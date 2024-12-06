package be.orbinson.aem.opentelemetry.core.filters;

import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryFactoryImpl;
import be.orbinson.aem.util.OpenTelemetryTest;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OpenTelemetryRequestFilterTest extends OpenTelemetryTest {

    @Mock
    private FilterChain filterChain;

    private OpenTelemetryRequestFilter filter;

    @Test
    void defaultConfig() throws ServletException, IOException {
        registerInjectActivateOpenTelemetryService();
        filter = context.registerInjectActivateService(new OpenTelemetryRequestFilter());

        SlingHttpServletRequest request = spy(new MockSlingHttpServletRequest(
                context.resourceResolver(), context.bundleContext()
        ));
        SlingHttpServletResponse response = new MockSlingHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(request);
    }

    @Test
    void configEnabled() throws ServletException, IOException {
        registerInjectActivateOpenTelemetryService(
                "enabled", true
        );
        context.registerInjectActivateService(new OpenTelemetryFactoryImpl());
        filter = context.registerInjectActivateService(new OpenTelemetryRequestFilter());

        MockSlingHttpServletRequest request = spy(new MockSlingHttpServletRequest(
                context.resourceResolver(), context.bundleContext()
        ));
        request.setHeader("X-Forwarded-Host", "local.dev");
        request.setHeader("X-Forwarded-Port", "8443");
        request.setQueryString("param=value");
        MockRequestPathInfo requestPathInfo = new MockRequestPathInfo(context.resourceResolver());
        requestPathInfo.setResourcePath("/content/deceuninck/nl/page");
        requestPathInfo.setExtension("html");
        requestPathInfo.setSelectorString("selector");
        requestPathInfo.setSuffix("/suffix");

        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        MockSlingHttpServletResponse response = new MockSlingHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
