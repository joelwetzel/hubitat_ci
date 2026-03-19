# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

A Groovy library for testing [Hubitat](https://hubitat.com/) home automation device drivers and apps locally and in CI. It uses `GroovyShell` to load Hubitat scripts into a sandboxed environment, automatically validates their structure, and provides an integration testing framework.

**Group/Artifact:** `me.biocomp.hubitat_ci:hubitat_ci`
**Current version:** defined in `build.gradle` (`version = 0.49`)
**Published to:** GitHub Packages (Maven)

## Commands

```bash
# Run all tests
./gradlew test

# Watch mode
./gradlew test --continuous

# Full check (tests + verification)
./gradlew check

# Build with sources and docs jars
./gradlew build sourcesJar groovydocJar

# Publish to GitHub Packages (requires GITHUB_ACTOR and GITHUB_TOKEN env vars)
./gradlew publish

# Publish to local Maven repo (for local testing before publishing)
./gradlew publishToMavenLocal
```

Test reports are generated at `build/reports/tests/test/index.html`.

**Note:** VS Code's Testing tab does not show Spock/Groovy tests. Run tests from the terminal or use IntelliJ IDEA for step-through debugging.

## Architecture

### Sandbox Pattern (entry points)

- `HubitatAppSandbox` — entry point for testing Hubitat apps
- `HubitatDeviceSandbox` — entry point for testing Hubitat device drivers

Both accept a `File` or `String` (inline script). The `run()` method compiles the script, constructs the script object, and runs validation. `run()` accepts named parameters:
- `api:` — mock implementation of `AppExecutor` or `DeviceExecutor`
- `validationFlags:` — list of `me.biocomp.hubitat_ci.validation.Flags` to disable specific validations
- `customizeScriptBeforeRun:` — closure receiving the script object before initialization

### Executor Interfaces

`AppExecutor` and `DeviceExecutor` interfaces define all Hubitat API methods callable from scripts. Scripts redirect unknown method calls to the executor. Use Spock's `Mock{}` to create test doubles.

For full integration tests, use `IntegrationAppExecutor` and `IntegrationDeviceExecutor` which simulate realistic behavior.

### Validation Chain

`ValidatorBase` → `AppValidator` / `DeviceValidator` handle structural validation. Compilation customizers intercept at compile time:
- `AddValidationAfterEachMethodCompilationCustomizer` — validates method calls
- `LoggingCompilationCustomizer` — captures log calls
- `RemovePrivateFromScriptCompilationCustomizer` — makes private methods testable

### Integration Testing Framework (`util/integration/`)

Base test classes for end-to-end behavioral tests:
- `IntegrationAppSpecification` / `IntegrationDeviceSpecification` — extend these in Spock tests
- `TimeKeeper` — controls mock time; advance it to trigger scheduled events without waiting
- `IntegrationScheduler` — captures and executes `runIn`, `runOnce`, cron callbacks
- Device fixtures (`util/device_fixtures/`) — pre-built realistic mock devices: `SwitchFixtureFactory`, `DimmerFixtureFactory`, `LockFixtureFactory`, etc.

### Class Loading

`SandboxClassLoader` maps Hubitat runtime classes (e.g., `hubitat.device.HubResponse`) to library mock equivalents (e.g., `me.biocomp.hubitat_ci.api.common_api.HubResponse`) at compile time. See its `mapClassName` method for the full replacement list.

### Capability System

`Capabilities` registry is generated from `hubitat_api.json`. `GeneratedCapability`, `GeneratedAttribute`, and `GeneratedCommand` provide definitions used during validation of driver commands and attributes.

## Key Package Layout

```
src/main/groovy/me/biocomp/hubitat_ci/
├── api/
│   ├── app_api/          # AppExecutor interface and app-specific APIs
│   ├── device_api/       # DeviceExecutor + extensive Z-Wave/ZigBee command classes
│   ├── common_api/       # Shared APIs (HTTP, logging, HubResponse, etc.)
│   └── hub/              # Hub-level APIs
├── app/                  # App sandbox, validator, preference/definition readers
├── device/               # Device sandbox, validator, metadata parsing
├── capabilities/         # Capability registry and generated classes
├── validation/           # ValidatorBase, Flags enum, named parameter validation
└── util/
    ├── integration/      # Integration testing framework
    ├── device_fixtures/  # Pre-built mock device factories
    └── SandboxClassLoader.groovy

src/test/groovy/me/biocomp/hubitat_ci/
```

## Testing

Tests use the [Spock framework](http://spockframework.org/) (BDD-style `given/when/then` or `expect` blocks). Test classes extend `spock.lang.Specification`.

The `src/test/` directory contains tests for the library itself. Git submodules in the repo root (`EcoNet/`, `konnected/`, etc.) are real-world Hubitat script repos used by `ExistingDeviceScriptsTest`.

## Publishing

To release a new version:
1. Update `version` in `build.gradle`
2. Test locally: `./gradlew publishToMavenLocal`
3. Merge to master — CI auto-detects new version and publishes to GitHub Packages

The CI pipeline (`.github/workflows/ci.yml`) checks whether the version already exists in GitHub Packages before publishing to avoid duplicate publishes.
