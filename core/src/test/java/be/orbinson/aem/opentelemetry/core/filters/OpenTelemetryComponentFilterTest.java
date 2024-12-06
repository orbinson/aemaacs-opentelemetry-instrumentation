package be.orbinson.aem.opentelemetry.core.filters;

import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryFactoryImpl;
import be.orbinson.aem.util.OpenTelemetryTest;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.ServletResolver;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OpenTelemetryComponentFilterTest extends OpenTelemetryTest {

    @Mock
    private FilterChain filterChain;

    private OpenTelemetryComponentFilter filter;

    @Mock
    private ServletResolver servletResolver;

    @BeforeEach
    void setUp() {
        servletResolver = context.registerService(ServletResolver.class, servletResolver);
    }

    @Test
    void defaultConfig() throws ServletException, IOException {
        registerInjectActivateOpenTelemetryService();
        filter = context.registerInjectActivateService(new OpenTelemetryComponentFilter());

        SlingHttpServletRequest request = spy(new MockSlingHttpServletRequest(
                context.resourceResolver(), context.bundleContext()
        ));
        SlingHttpServletResponse response = new MockSlingHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(request);
    }

    @Test
    void enabledWithoutComponents() throws ServletException, IOException {
        registerInjectActivateOpenTelemetryService(
                "enabled", true,
                "endpoint", "http://collector:4318"
        );
        filter = context.registerInjectActivateService(new OpenTelemetryComponentFilter());

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
                "enabled", true,
                "traceComponents", true
        );
        context.registerInjectActivateService(new OpenTelemetryFactoryImpl());
        filter = context.registerInjectActivateService(new OpenTelemetryComponentFilter());

        MockSlingHttpServletRequest request = spy(new MockSlingHttpServletRequest(
                context.resourceResolver(), context.bundleContext()
        ));
        Servlet mockServlet = mock(Servlet.class);
        ServletConfig mockServletConfig = mock(ServletConfig.class);

        when(servletResolver.resolveServlet(request)).thenReturn(mockServlet);
        when(mockServlet.getServletConfig()).thenReturn(mockServletConfig);
        when(mockServletConfig.getServletName()).thenReturn("PageServlet");
        MockSlingHttpServletResponse response = new MockSlingHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
