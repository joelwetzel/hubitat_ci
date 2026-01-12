# Hubitat CI Development Guide

## Project Overview

Hubitat CI is a **testing framework** for Hubitat home automation apps and device drivers. It enables local testing and CI/CD validation of Groovy scripts without deploying to physical Hubitat hubs. The framework uses GroovyShell to load scripts as objects, validates metadata/capabilities automatically, and provides integration testing with time control and device fixtures.

## Architecture

### Core Components

- **Sandbox Classes** ([HubitatAppSandbox.groovy](../src/main/groovy/me/biocomp/hubitat_ci/app/HubitatAppSandbox.groovy), [HubitatDeviceSandbox.groovy](../src/main/groovy/me/biocomp/hubitat_ci/device/HubitatDeviceSandbox.groovy)): Parse and execute Hubitat scripts in isolated environments
- **Integration Testing Framework** ([util/integration/](../src/main/groovy/me/biocomp/hubitat_ci/util/integration/)): Provides `IntegrationAppSpecification` and `IntegrationDeviceSpecification` base classes for end-to-end tests
- **Device Fixtures** ([util/device_fixtures/](../src/main/groovy/me/biocomp/hubitat_ci/util/device_fixtures/)): Mock devices (switches, dimmers, locks, etc.) that simulate realistic behavior
- **TimeKeeper & IntegrationScheduler** ([TimeKeeper.groovy](../src/main/groovy/me/biocomp/hubitat_ci/util/integration/TimeKeeper.groovy)): Control time in tests to trigger scheduled events without waiting

### Key Design Patterns

**Script-as-Text Testing**: Most tests use script text as input rather than depending on internal classes. This keeps tests resilient to refactoring. Example from tests:
```groovy
def scriptText = '''
definition(name: "Test App") { ... }
preferences { ... }
'''
def sandbox = new HubitatAppSandbox(scriptText)
```

**Two Testing Modes**:
1. **Unit Testing**: Use sandboxes directly with mocks for fine-grained control
2. **Integration Testing**: Extend `IntegrationAppSpecification` for end-to-end behavior testing

## Development Workflows

### Running Tests

```bash
# Run all tests
./gradlew test

# Continuous testing (watches for changes)
./gradlew test --continuous

# Full verification (tests + checks)
./gradlew check
```

Test reports: `build/reports/tests/test/index.html`

**VS Code Note**: The Testing tab doesn't show Spock tests. Use tasks ("gradle: test") or terminal instead. IntelliJ IDEA provides better Spock support.

### Building & Publishing

```bash
# Build library
./gradlew build

# Generate docs
./gradlew groovydoc  # Output: build/docs/groovydoc/

# Publish to GitHub Packages (requires GitHub token with write:packages scope)
./gradlew publish
```

GitHub Actions automatically builds and publishes on every master commit ([.github/workflows/ci.yml](../.github/workflows/ci.yml)).

## Project-Specific Conventions

### Hubitat Script Structure

Apps and drivers use `definition()` blocks for metadata and `preferences` for UI:
```groovy
definition(
    name: 'My App',
    namespace: 'example',
    author: 'Author'
)

preferences {
    page(name: 'mainPage') {
        section() {
            input name: 'switches', type: 'capability.switch', multiple: true
        }
    }
}

def installed() { initialize() }
def updated() { unsubscribe(); initialize() }
def initialize() { subscribe(switches, 'switch', handler) }
```

### Validation Flags

Control sandbox behavior with `Flags` enum in test options:
- `Flags.DontRunScript`: Compile only, don't execute
- `Flags.DontValidateDefinition`: Skip definition validation
- Pass via `validationFlags` option to sandbox.run()

### Integration Test Pattern

```groovy
class MyAppTest extends IntegrationAppSpecification {
    def switchFixture = SwitchFixtureFactory.create('s1')

    def setup() {
        TimeKeeper.set(Date.parse("yyyy-MM-dd HH:mm:ss", "2024-01-01 10:00:00"))
        super.initializeEnvironment(
            appScriptFilename: "Scripts/MyApp.groovy",
            userSettingValues: [switches: [switchFixture]]
        )
        switchFixture.initialize(appExecutor, [switch: "off"])
    }

    def "test scheduled behavior"() {
        given: appScript.initialize()
        when: TimeKeeper.advanceMinutes(5)
        then: 1 * log.debug("Scheduled callback fired")
    }
}
```

### API Override Files

- `hubitat_api.json`: Auto-exported Hubitat API signatures (empty in repo, populated by exporter scripts)
- `hubitat_api_overrides.json`: Manual API corrections/extensions for validation

## File Organization

- `Scripts/`: Example Hubitat apps/drivers for testing the framework
- `SubmodulesWithScripts/`: Git submodules with real-world Hubitat projects
- `src/main/groovy/me/biocomp/hubitat_ci/`: Framework source
  - `api/`: API interfaces (AppExecutor, DeviceExecutor)
  - `app/`, `device/`: Sandbox implementations
  - `util/integration/`: Integration testing framework
  - `validation/`: Metadata/API validators
- `src/test/groovy/`: Framework tests using Spock

## Testing Guidelines

1. **Write tests using script text** when possible to avoid coupling to internal classes
2. **For integration tests**, extend `IntegrationAppSpecification`/`IntegrationDeviceSpecification` - don't create sandboxes manually
3. **Use TimeKeeper for time manipulation** instead of Thread.sleep() or actual delays
4. **One or two strategic assertions per test** - integration tests should verify behavior, not implementation
5. **Add tests for new features** as required by [contributing.md](../contributing.md)

## Common Pitfalls

- **Don't run Jupyter/Notebook commands** - this is a Groovy/Gradle project, not Python
- **Markdown cells can't be executed** in test specs
- **VS Code Testing tab won't show tests** - Spock requires JUnit-compatible runners
- **TimeKeeper must be reset** between tests - use `TimeKeeper.removeAllListeners()` in setup/cleanup
- **Device fixtures must be initialized** with executor and initial state before use

## Key Resources

- [Getting Started Guide](../docs/getting_started.md)
- [How to Test Documentation](../docs/how_to_test.md)
- [Integration Testing Guide](../docs/integration_testing.md)
- [Example Project](https://github.com/biocomp/hubitat_ci_example)
