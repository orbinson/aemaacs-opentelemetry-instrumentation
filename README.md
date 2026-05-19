# AEMaaCS OpenTelemetry Instrumentation

While AEMaaCS does not support injecting a JavaAgent to enable auto instrumentation
and [Sling instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation/pull/9469) is not yet
merged into OpenTelemetry this bundle can be used to add basic instrumentation to your application.

This bundle has three main features:

* Add traces for HTTP requests and AEM components.
* Configure a logging bridge for AEM logs.
* Provide an instrumented Apache HttpClient.

The vendor code provided by OpenTelemetry is provided by
an [OSGi wrapper](https://github.com/orbinson/opentelemetry-osgi-wrappers).

## Configuration

To [autoconfigure](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md)
the OpenTelemetry SDK you need to provide the following environment variables.
The example uses the http exporter.

```text
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318
OTEL_SERVICE_NAME=aem-author
```

For local debugging, add the `opentelemetry-exporter-logging` bundle to your `all` package in start level `15` with a specific profile for
local development, so that you can use the logging exporter to print to the console.

```text
OTEL_TRACES_EXPORTER=logging
OTEL_METRICS_EXPORTER=logging
OTEL_LOGS_EXPORTER=logging
OTEL_SERVICE_NAME=aem-author
```

The AEMaaCS OpenTelemetry Instrumentation bundle can be configured with an OSGi configurations with
pid `be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl`

| property                 | description                                                                 | default    |
|--------------------------|-----------------------------------------------------------------------------|------------|
| enabled                  | Enable or disable telemetry                                                 | `false`    |
| enableLogAppender        | Forwards AEM logs to OpenTelemetry via a Logback appender                   | `false`    |
| instrumentationScopeName | Instrumentation scope name for spans                                        | `aem`      |
| loggerNames              | Logback loggers to attach the appender to (Sling AppenderTracker filter)    | `["ROOT"]` |
| traceComponents          | A span for AEM components                                                   | `false`    |
| useGlobalOpenTelemetry   | Use global OpenTelemetry object                                             | `false`    |

### Forwarding additional log files

By default `loggerNames=["ROOT"]` captures every logger that propagates to ROOT,
which is the source of `error.log`. AEM also configures several loggers with
`additivity=false` so their events never reach ROOT — to forward those too, add
them to `loggerNames`:

| logger                                       | file                |
|----------------------------------------------|---------------------|
| `log.request`                                | `request.log`       |
| `log.access`                                 | `access.log`        |
| `log.history`                                | `history.log`       |
| `com.adobe.granite.audit`                    | `auditlog.log`      |
| `org.apache.jackrabbit.core.audit`           | `audit.log`         |
| `org.apache.jackrabbit.oak.audit`            | `audit.log`         |
| `org.apache.jackrabbit.oak.query.stats.QueryRecorder` | `queryrecorder.log` (DEBUG, noisy) |

To capture everything except the very noisy DEBUG QueryRecorder:

```
loggerNames=["ROOT", "log.request", "log.access", "log.history", \
             "com.adobe.granite.audit", \
             "org.apache.jackrabbit.core.audit", \
             "org.apache.jackrabbit.oak.audit"]
```

### Logback resilience

The appender is the only component that touches Logback classes. Logback is
declared as `DynamicImport-Package` in the core bundle manifest — meaning the
bundle starts and resolves cleanly even if Logback is removed from a future
AEMaaCS release. In that scenario DS will leave `OtelLogbackAppender`
unsatisfied while every other component (request filter, OTel factory,
component filter, etc.) keeps working unchanged.

### Manual end-to-end verification

A docker-compose collector that prints received signals to stdout lives under
`test/otel-collector/`. See its README for how to start it and point AEM at it.

## Installation

Three content packages are provided that can be used.

The `all` package, which you will be using in most case, containing the `minimal` and the `opentelemetry-okhttp-exporter` packages.

```xml
<dependency>
    <groupId>be.orbinson.aem</groupId>
    <artifactId>aemaacs-opentelemetry-instrumentation.all</artifactId>
</dependency>
```

The `minimal` package containing the AEMaaCS-specific classes to add instumentation.

```xml
<dependency>
    <groupId>be.orbinson.aem</groupId>
    <artifactId>aemaacs-opentelemetry-instrumentation.minimal</artifactId>
</dependency>
```

The `opentelemetry-okhttp-exporter` package that contains all the OSGi wrapped dependencies.

```xml
<dependency>
    <groupId>be.orbinson.aem</groupId>
    <artifactId>aemaacs-opentelemetry-instrumentation.opentelemetry-okhttp-exporter</artifactId>
</dependency>
```

## Instrumented Apache HttpClient

To use an instrumented Apache HttpClient you can use the `io.opentelemetry.instrumentation.apachehttpclient.v4_3.ApacheHttpClientTelemetry`.

```java
// Inject OpenTelemetry service
@OSGiService
private OpenTelemetryFactory openTelemetryFactory;

try(CloseableHttpClient client = ApacheHttpClientTelemetry.create(openTelemetryFactory.get()).newHttpClient()) {
    // Create and send a request
    HttpGet httpGet = new HttpGet("http://host/endpoint");

    try(CloseableHttpResponse response = client.execute(httpGet)) {
        if (response.getStatusLine().getStatusCode() == 200) {
            // OK
        }
    }
} catch (IOException e) {
    // Handle exception
}
```
