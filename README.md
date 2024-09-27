# AEMaaCS OpenTelemetry Instrumentation

While AEMaaCS does not support injecting a JavaAgent to enable auto instrumentation
and [Sling instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation/pull/9469) is not yet
merged into OpenTelemetry this bundle can be used to add basic instrumentation to your application.

This bundle has two main features:

* Add traces for HTTP requests and AEM components.
* Configure a logging bridge for AEM logs.

The vendor code provided by OpenTelemetry is provided by
an [OSGi wrapper](https://github.com/orbinson/opentelemetry-osgi-wrappers).

## Configuration

To [autoconfigure](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md)
the OpenTelemetry SDK you need to provide the following environment variables.

```text
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
OTEL_RESOURCE_ATTRIBUTES=service.name=aem-author
```

If you need configure the HTTP exporter you need to set the following environment

```text
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318
OTEL_RESOURCE_ATTRIBUTES=service.name=aem-author
```

For local debugging you can use the logging exporter to print to the console

```text
OTEL_TRACES_EXPORTER=logging
OTEL_METRICS_EXPORTER=logging
OTEL_LOGS_EXPORTER=logging
OTEL_RESOURCE_ATTRIBUTES=service.name=aem-author
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

The first option is using the `minimal` package that will only install the AEMaaCS-specific classes to your AEM

```xml

<dependency>
    <groupId>be.orbinson.aem</groupId>
    <artifactId>aemaacs-opentelemetry-instrumentation.minimal</artifactId>
</dependency>
```

The second option is using the `all` package, that will also install all required OpenTelemetry OSGi wrappers through
the `opentelemetry-okhttp` content package so that you use the API and SDK classes together with OkHttp as exporter

```xml

<dependency>
    <groupId>be.orbinson.aem</groupId>
    <artifactId>aemaacs-opentelemetry-instrumentation.all</artifactId>
</dependency>
```
