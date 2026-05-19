# Local OTel collector for manual end-to-end verification

This is a minimal OpenTelemetry collector that prints every received signal
(traces, metrics, logs) to its container stdout. Useful when changing the
appender or the OTel factory to confirm AEM is shipping data end-to-end.

The unit + integration tests in `core/src/test/java` cover the appender's
logic without docker. Use this when you need to look at what AEM is
actually putting on the wire against a real OTLP receiver.

## Start

```bash
cd test/otel-collector
docker compose up -d
```

Listens on:
- `localhost:4317` — OTLP gRPC (default for AEM SDK)
- `localhost:4318` — OTLP HTTP

## Tail received signals

```bash
docker logs -f otel-collector
```

## Configure AEM to send to it

OSGi PID `be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl`:

```
enabled=true
enableLogAppender=true
loggerNames=["ROOT"]
```

The SDK defaults send to `http://localhost:4317` (gRPC). To override, set the
standard OTel env vars on the JVM (e.g. `OTEL_EXPORTER_OTLP_ENDPOINT`).

## Stop

```bash
docker compose down
```
