package be.orbinson.aem.opentelemetry.core.filters;

import be.orbinson.aem.opentelemetry.core.ContextPropagator;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryConfig;
import be.orbinson.aem.opentelemetry.services.api.OpenTelemetryFactory;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.semconv.SemanticAttributes;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
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
import java.util.stream.Collectors;

@Component
@ServiceRanking(Integer.MAX_VALUE)
@SlingServletFilter(scope = SlingServletFilterScope.REQUEST)
public class OpenTelemetryRequestFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(OpenTelemetryRequestFilter.class);

    @Reference
    private OpenTelemetryConfig config;

    @Reference
    private OpenTelemetryFactory openTelemetryFactory;

    private Tracer tracer;
    private TextMapPropagator textMapPropagator;

    @Activate
    protected void activate() {
        if (config.enabled()) {
            log.debug("OpenTelemetry instrumentation enabled");
            OpenTelemetry openTelemetry = openTelemetryFactory.get();
            tracer = openTelemetry.getTracer(config.instrumentationScopeName());
            textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();
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
        log.trace("Start request filter");

        if (!config.enabled()) {
            log.trace("OpenTelemetry is not enabled, skipping filter");
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            SlingHttpServletRequest request = (SlingHttpServletRequest) servletRequest;
            SlingHttpServletResponse response = (SlingHttpServletResponse) servletResponse;
            String route = getRoute(request.getRequestPathInfo());

            if (route != null) {
                log.trace("Trace request with route '{}'", route);
                String spanName = request.getMethod() + " " + route;
                filterWithSpan(servletRequest, servletResponse, filterChain, spanName, request, response);
            }
        }

        log.trace("End request filter");
    }

    private void filterWithSpan(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain,
            String spanName, SlingHttpServletRequest request, SlingHttpServletResponse response
    ) throws IOException, ServletException {
        log.trace("Start span with name '{}'", spanName);

        Context extractedContext = textMapPropagator.extract(Context.current(), request, ContextPropagator.getter);
        try (Scope ignoredContextScope = extractedContext.makeCurrent()) {
            Span serverSpan = tracer.spanBuilder(spanName)
                    .setSpanKind(SpanKind.SERVER)
                    .startSpan();

            setRequestSpanAttributes(serverSpan, request);
            try (Scope ignoredSpanScope = serverSpan.makeCurrent()) {
                filterChain.doFilter(servletRequest, servletResponse);
                setResponseSpanAttributes(serverSpan, response);
            } catch (IOException e) {
                serverSpan.recordException(e);
                throw e;
            } finally {
                serverSpan.end();
            }
        }

        log.trace("End span with name '{}", spanName);
    }

    private static void setRequestSpanAttributes(Span serverSpan, SlingHttpServletRequest request) {
        serverSpan.setAttribute(SemanticAttributes.HTTP_REQUEST_METHOD, request.getMethod());
        serverSpan.setAttribute(SemanticAttributes.HTTP_ROUTE, getRoute(request.getRequestPathInfo()));
        serverSpan.setAttribute(SemanticAttributes.URL_PATH, request.getRequestURI());
        serverSpan.setAttribute(SemanticAttributes.URL_SCHEME, request.isSecure() ? "https" : "http");
        if (!request.getRequestParameterList().isEmpty()) {
            String requestParameters = request.getRequestParameterList().stream()
                    .map(requestParameter -> requestParameter.getName() + "=" + requestParameter.getString())
                    .collect(Collectors.joining("&"));
            serverSpan.setAttribute(SemanticAttributes.URL_QUERY, requestParameters);
        }
        serverSpan.setAttribute(SemanticAttributes.SERVER_ADDRESS, getHost(request));
        serverSpan.setAttribute(SemanticAttributes.SERVER_PORT, getPort(request));
    }

    private static String getHost(SlingHttpServletRequest request) {
        String value = request.getHeader("X-Forwarded-Host");
        return StringUtils.defaultIfBlank(value, request.getServerName());
    }

    private static long getPort(SlingHttpServletRequest request) {
        String value = request.getHeader("X-Forwarded-Port");
        if (StringUtils.isNotBlank(value)) {
            return Integer.parseInt(value);
        }
        return request.getServerPort();
    }

    private static void setResponseSpanAttributes(Span serverSpan, SlingHttpServletResponse response) {
        serverSpan.setAttribute("http.response.mime_type", response.getContentType());
        serverSpan.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, response.getStatus());
        if (response.getStatus() >= 500) {
            serverSpan.setStatus(StatusCode.ERROR);
        }
    }

    private static String getRoute(RequestPathInfo pathInfo) {
        String result = pathInfo.getResourcePath();
        if (StringUtils.isNotBlank(pathInfo.getSelectorString())) {
            result += "." + pathInfo.getSelectorString();
        }
        if (StringUtils.isNotBlank(pathInfo.getExtension())) {
            result += "." + pathInfo.getExtension();
        }
        if (StringUtils.isNotBlank(pathInfo.getSuffix())) {
            result += pathInfo.getSuffix();
        }
        return result;
    }

    @Override
    public void destroy() {
        // no-op
    }
}
