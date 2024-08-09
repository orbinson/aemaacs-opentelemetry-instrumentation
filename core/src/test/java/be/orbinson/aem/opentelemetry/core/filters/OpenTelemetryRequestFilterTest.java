package be.orbinson.aem.opentelemetry.core.filters;

import be.orbinson.aem.opentelemetry.core.filters.OpenTelemetryRequestFilter;
import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl;
import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryFactoryImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class OpenTelemetryRequestFilterTest {
    @Mock
    private FilterChain filterChain;

    private OpenTelemetryRequestFilter filter;

    private final AemContext context = new AemContext();

    @Test
    void defaultConfig() throws ServletException, IOException {
        context.registerInjectActivateService(new OpenTelemetryConfigImpl());
        context.registerInjectActivateService(new OpenTelemetryFactoryImpl());
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
        context.registerInjectActivateService(new OpenTelemetryConfigImpl(),
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
