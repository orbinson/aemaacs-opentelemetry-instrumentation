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

For local debugging you can use the logging exporter to print to the console.

```text
OTEL_TRACES_EXPORTER=logging
OTEL_METRICS_EXPORTER=logging
OTEL_LOGS_EXPORTER=logging
OTEL_SERVICE_NAME=aem-author
```

The AEMaaCS OpenTelemetry Instrumentation bundle can be configured with an OSGi configurations with
pid `be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl`

| property                 | description                                          | default    |
|--------------------------|------------------------------------------------------|------------|
| enableLogAppender        | Forwards AEM logs                                    | `false`    |
| enabled                  | Enable or disable telemetry                          | `false`    |
| instrumentationScopeName | Instrumentation scope name for spans                 | `aem`      |
| loggerNames              | Logger to forward when `enableLogAppender` is `true` | `["ROOT"]` |
| traceComponents          | A span for AEM components                            | `false`    |
| useGlobalOpenTelemetry   | Use global OpenTelemetry object                      | `false`    |

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
