# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.2.0] - 2026-05-19

- Replace deprecated programmatic Logback attachment with an `OtelLogbackAppender` service.
  Logback is now imported via `DynamicImport-Package` so the bundle keeps working if Logback
  disappears from a future AEMaaCS release. The appender registers itself with Sling's
  AppenderTracker via the `loggers` service property derived from `loggerNames`.
- Skip log events from `io.opentelemetry.*` and `be.orbinson.aem.opentelemetry.*` to prevent
  re-entrant emission when the exporter itself logs errors.
- API package bumped to `2.0.0` because of the new logging integration model.
- Drop the `com.adobe.aem:aem-sdk-api` dependency and the `io.wcm.maven.aem-cloud-dependencies`
  BOM. The bundle now compiles against the bare minimum (Sling API 2.27.6, Sling Servlets
  Annotations 1.2.6, Servlet 3.1.0, OSGi R7 annotations, SLF4J 1.7.32, Logback 1.2.13
  compile-only). Versions are pinned at or below the wcm-io AEM 6.5 BOM, so the bundle
  is binary-compatible with both AEMaaCS and AEM 6.5.
- Inline the two `commons-lang3.StringUtils` helpers (`isNotBlank`, `defaultIfBlank`) so
  commons-lang3 is no longer a runtime requirement.
- Switch unit tests from `io.wcm.testing.aem-mock.junit5` to `org.apache.sling.testing.sling-mock.junit5`
  (3.6.2, last javax-servlet line). Removes the only test-time dependency on AEM classes.

## [1.1.0] - 2026-03-21

- Upgrade to latest opentelemetry versions
- Make logstash imports optional

## [1.0.2] - 2024-10-23

## [1.0.1] - 2024-10-19

- Ensure OpenTelemetry bundles are available at bundle startup

## [1.0.0] - 2024-10-18

- Initial release with OpenTelemetry 1.43.0 SDK and dependencies
- Instrumented Apache HttpClient 4.3
- Logging bridge for using the Logback Appender 1.0

[unreleased]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.2.0...HEAD
[1.2.0]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.1.0...1.2.0
[1.1.0]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.2...1.1.0
[1.0.2]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/8105cce5a1ca5965f633503305ce800d11b5ab2d...1.0.0
