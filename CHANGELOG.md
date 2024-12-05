# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Add API for adding metrics 
- Add DistributionQueue size gauge

## [1.0.2] - 2024-10-23

### Removed

- Remove opentelemetry-exporter-logging

## [1.0.1] - 2024-10-19

### Fixed

- Ensure OpenTelemetry bundles are available at bundle startup

## [1.0.0] - 2024-10-18

### Added

- Initial release with OpenTelemetry 1.43.0 SDK and dependencies
- Instrumented Apache HttpClient 4.3
- Logging bridge for using the Logback Appender 1.0

[unreleased]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.2...HEAD
[1.0.2]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.1...1.0.2
[1.0.1]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/orbinson/aemaacs-opentelemetry-instrumentation/compare/8105cce5a1ca5965f633503305ce800d11b5ab2d...1.0.0
