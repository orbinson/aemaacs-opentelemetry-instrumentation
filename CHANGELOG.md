# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Replace deprecated programmatic Logback attachment with an `OtelLogbackAppender` service.
  Logback is now imported via `DynamicImport-Package` so the bundle keeps working if Logback
  disappears from a future AEMaaCS release. The appender registers itself with Sling's
  AppenderTracker via the `loggers` service property derived from `loggerNames`.
- Skip log events from `io.opentelemetry.*` and `be.orbinson.aem.opentelemetry.*` to prevent
  re-entrant emission when the exporter itself logs errors.
- API package bumped to `2.0.0` because of the new logging integration model.

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

[unreleased]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.1.0...HEAD
[1.1.0]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.2...1.1.0
[1.0.2]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/8105cce5a1ca5965f633503305ce800d11b5ab2d...1.0.0
