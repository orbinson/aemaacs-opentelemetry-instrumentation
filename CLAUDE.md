# CLAUDE.md

## Project Overview

AEMaaCS OpenTelemetry Instrumentation - an OSGi bundle providing OpenTelemetry instrumentation for Adobe Experience Manager as a Cloud Service (AEMaaCS). Since AEMaaCS does not support Java agents, this project provides HTTP request tracing, AEM component span tracking, and an instrumented Apache HttpClient.

OSGi wrapper dependencies are maintained in a separate repository: https://github.com/orbinson/opentelemetry-osgi-wrappers

## Build Commands

```bash
# Full build with all quality checks (default)
mvn clean install

# Quick build - skip all validation and tests
mvn clean install -PquickBuild

# Run tests only for core module
mvn test -pl core

# Install bundle to local AEM author (localhost:4502, has to be turned on manually)
mvn clean install -PautoInstallBundle

# Install content package to local AEM author
mvn clean install -PautoInstallPackage

# Install content package to local AEM publish (localhost:4503)
mvn clean install -PautoInstallPackagePublish
```

## Module Structure

| Module | Type | Purpose |
|--------|------|---------|
| `api` | OSGi bundle | Service interfaces (`OpenTelemetryFactory`, `OpenTelemetryConfig`) |
| `core` | OSGi bundle | Implementation: filters, processors, services |
| `minimal` | Content package | Embeds api + core bundles |
| `all` | Content package | Embeds minimal + opentelemetry-okhttp-exporter |
| `opentelemetry-okhttp-exporter` | Content package | 27+ OSGi-wrapped OpenTelemetry dependencies |

## Code Conventions

- **Java version**: 11 (source/target)
- **Style**: Google Java Style (modified) - enforced by Checkstyle (`checkstyle.xml`)
- **Line length**: 150 characters max (Java), 120 (editorconfig)
- **Indentation**: 4 spaces, 8 for continuation
- **Line endings**: LF only
- **Encoding**: UTF-8
- **Javadoc**: Not mandatory on types/methods
- **Star imports**: Allowed for class imports

## Quality Gates

The `codeQuality` profile (active by default) enforces:

- **Checkstyle** - `failOnViolation=true`
- **SpotBugs** - Static bug detection
- **PMD** - Code analysis (targetJdk=11)
- **JaCoCo** - Coverage: 80% line, 70% method minimum
  - Excluded from coverage: `OpenTelemetryFactoryImpl`

## Key Architecture Patterns

- **OSGi services**: Use `@Component`, `@Designate` for configuration, `@ServiceConsumer` for dependencies
- **Sling filters**: `@SlingServletFilter` with `@ServiceRanking(Integer.MAX_VALUE)` for request interception
- **API/Impl separation**: Interfaces in `api` module with `@ProviderType`, implementations in `core`
- **Package structure**:
  - `be.orbinson.aem.opentelemetry.services.api` - Service interfaces
  - `be.orbinson.aem.opentelemetry.core` - Utilities
  - `be.orbinson.aem.opentelemetry.core.filters` - Sling servlet filters
  - `be.orbinson.aem.opentelemetry.core.services` - Log processors
  - `be.orbinson.aem.opentelemetry.core.services.impl` - Service implementations

## Testing

- **Framework**: JUnit 5 + Mockito 4.1.0 + io.wcm AEM Mock
- **Run**: `mvn test` or `mvn test -pl core`
- **Surefire** configured with `--add-opens` for JDK internal Xerces (AEM SDK 2026.2 compatibility)

## Version Properties (root pom.xml)

| Property | Current Value |
|----------|--------------|
| `opentelemetry.version` | 1.60.1 |
| `opentelemetry.instrumentation.version` | 2.26.0 |
| `opentelemetry.semconv.version` | 1.40.0 |
| `aem.sdk.api` | 2026.2.24678.20260226T154829Z-260200 |

## Release Process

- Releases go to Maven Central via Sonatype Nexus Staging
- GitHub Actions workflow: `.github/workflows/release.yml` (manual dispatch)
- Uses `maven-release-plugin` with `release` profile (attaches sources + javadoc)
- Version bumps follow semantic versioning; current development: `1.0.3-SNAPSHOT`

## OSGi Bundle Configuration

- **BND plugin** (6.4.0) generates OSGi manifests
- **Bundle category**: Orbinson
- **Start levels**: SpiFly/ASM at level 2, OpenTelemetry wrappers at level 15
- **Config PID**: `be.orbinson.aem.opentelemetry.core.services.impl.OpenTelemetryConfigImpl`
