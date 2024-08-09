package be.orbinson.aem.opentelemetry.core.filters;

import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.servlets.ServletResolver;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

@Component
@ServiceRanking(Integer.MAX_VALUE)
@SlingServletFilter(scope = SlingServletFilterScope.COMPONENT)
public class OpenTelemetryComponentFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(OpenTelemetryComponentFilter.class);

    @Reference
    private OpenTelemetryConfig config;

    @Reference
    private OpenTelemetryFactory openTelemetryFactory;

    @Reference
    private ServletResolver servletResolver;

    private Tracer tracer;

    @Activate
    protected void activate() {
        if (config.enabled()) {
            tracer = openTelemetryFactory.get().getTracer(config.instrumentationScopeName());
        }
    }

    @Override
    public void init(FilterConfig paramFilterConfig) {
        // no-op
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain
    ) throws IOException, ServletException {
        log.trace("Start component filter");

        if (!config.enabled() || !config.traceComponents()) {
            log.trace("Components are not configured to be instrumented");
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            Servlet servlet = servletResolver.resolveServlet((SlingHttpServletRequest) servletRequest);
            if (servlet != null) {
                filterWithSpan(servletRequest, servletResponse, filterChain, servlet);
            }
        }

        log.trace("End component filter");
    }

    private void filterWithSpan(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain, Servlet servlet
    ) throws ServletException, IOException {
        String spanName = servlet.getServletConfig().getServletName();
        log.trace("Start span with name '{}'", spanName);

        Span componentSpan = tracer
                .spanBuilder(spanName)
                .startSpan();
        try (Scope ignoredScope = componentSpan.makeCurrent()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (IOException e) {
            componentSpan.recordException(e);
            throw e;
        } finally {
            componentSpan.end();
        }

        log.trace("End span with name '{}'", spanName);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
