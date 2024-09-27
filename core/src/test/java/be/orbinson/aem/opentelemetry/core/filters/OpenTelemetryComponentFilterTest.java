package be.orbinson.aem.opentelemetry.core.filters;

import be.orbinson.aem.opentelemetry.core.filters.OpenTelemetryComponentFilter;
import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl;
import be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryFactoryImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.ServletResolver;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class OpenTelemetryComponentFilterTest {
    @Mock
    private FilterChain filterChain;

    private OpenTelemetryComponentFilter filter;

    private final AemContext context = new AemContext();

    @Mock
    private ServletResolver servletResolver;

    // Disable exporting
    @BeforeAll
    static void beforeAll() {
        System.setProperty("otel.metrics.exporter", "none");
        System.setProperty("otel.traces.exporter", "none");
        System.setProperty("otel.logs.exporter", "none");
    }
    
    @BeforeEach
    void setUp() {
        servletResolver = context.registerService(ServletResolver.class, servletResolver);
    }

    @Test
    void defaultConfig() throws ServletException, IOException {
        context.registerInjectActivateService(new OpenTelemetryConfigImpl());
        context.registerInjectActivateService(new OpenTelemetryFactoryImpl());
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
        context.registerInjectActivateService(new OpenTelemetryConfigImpl(),
                "enabled", true,
                "endpoint", "http://collector:4318"
        );
        context.registerInjectActivateService(new OpenTelemetryFactoryImpl());
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
        context.registerInjectActivateService(new OpenTelemetryConfigImpl(),
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
