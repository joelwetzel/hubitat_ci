# Hubitat CI

[![CI/CD Pipeline](https://github.com/joelwetzel/hubitat_ci/actions/workflows/ci.yml/badge.svg)](https://github.com/joelwetzel/hubitat_ci/actions/workflows/ci.yml)
[![Latest Release](https://img.shields.io/badge/version-0.49-blue)](https://github.com/joelwetzel/hubitat_ci/packages/2804977)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This library allows testing of Hubitat scripts locally on your machine (and via **C**ontinuous **I**ntegration, thus Hubitat CI).

It uses [GroovyShell](http://docs.groovy-lang.org/latest/html/api/groovy/lang/GroovyShell.html) to load the scripts, makes objects out of them and lets you test them in two complementary ways:

1. **Validation Tests** - Automatically validate script structure, metadata, preferences, capabilities, and API usage
2. **Integration Tests** - Test actual app/driver behavior by simulating devices, events, and time advancement

## About This Fork

This repository is a fork of the original [biocomp/hubitat_ci](https://github.com/biocomp/hubitat_ci) project with added integration testing capabilities. The integration testing framework allows you to write realistic end-to-end tests that simulate how your apps and drivers behave in a real Hubitat environment without needing physical hardware.

## Two Types of Testing

### 1. Validation Tests - Catch Structural Errors Automatically

Validation tests automatically check your script's structure, metadata, and API usage **without you writing test code**. These tests run when you create a sandbox and call `.run()` on it.

**What gets validated:**
- Definition blocks (name, namespace, author, etc.)
- Preferences structure (pages, sections, inputs)
- Input types and options (catching errors like using 'int' instead of 'number')
- Capability declarations and command signatures
- API method calls against known Hubitat APIs
- Prevention of unsupported or dangerous patterns

**Real-world catches:**
- [using 'int' instead of 'number' for input type](https://github.com/bspranger/Hubitat_iComfort/pull/5/commits/ebc2fa7ef38d41412fffe59da969ea97a2235334)
- [removing accidental writes to global state](https://github.com/bspranger/Hubitat_iComfort/pull/4/commits/48283ff2393a6bb9d65e7536be8952f2ffa90a71)
- [detecting unsupported parameters](https://github.com/mihaca/homeremote/pull/1/commits/2191d06101185170afa7eed2ae73a34de4bfdc1a)
- Use of [unsupported APIs](https://docs.smartthings.com/en/latest/getting-started/groovy-for-smartthings.html#restricted-methods)

See [How to Test](docs/how_to_test.md) for details on validation testing and advanced mocking techniques.

### 2. Integration Tests - Test Real Behavior

Integration tests let you write tests that verify how your app or driver **actually behaves** when devices change state, time passes, or events occur. Think of these as end-to-end tests that simulate a real Hubitat environment.

**What you can test:**
- **Time-based logic**: Advance time to test scheduled events, cron expressions, runIn callbacks
- **Device interactions**: Use realistic device fixtures (switches, dimmers, locks, etc.)
- **Event handling**: Trigger device events and verify your app responds correctly
- **State management**: Test that your app maintains state correctly across events
- **Complex scenarios**: Multi-device coordination, mode changes, time-of-day logic

**Integration test framework provides:**
- `TimeKeeper` - Control time in tests without waiting
- Device fixtures - Pre-built mock devices that behave realistically
- `IntegrationAppSpecification` and `IntegrationDeviceSpecification` base classes
- Automatic scheduler simulation for all Hubitat scheduling methods

See the [Integration Testing Guide](docs/integration_testing.md) for examples and best practices.

### Debug your script
With proper IDE (I'm using [IntellijIDEA](https://www.jetbrains.com/idea/)), you can step through your tests **and your script**, view variables and have rich debugging experience in general.

![Image of debug session](docs/debugging.png)

### Run your tests in the cloud after (or before) every push
Just something obvious, really.
If you can run it on your machine, you can also run it in the cloud.

This library, and [hubitat_ci_example](https://github.com/biocomp/hubitat_ci_example), for example,
have automatic builds set up using GitHub Actions.
Here's the workflow file: [.github/workflows/ci.yml](.github/workflows/ci.yml).

## Documentation

### Quick Start
- [Getting started](docs/getting_started.md) - Set up your first test project

### Testing Guides
- [How to test](docs/how_to_test.md) - Validation tests, mocking, and unit testing techniques
- [Integration testing guide](docs/integration_testing.md) - End-to-end behavior testing with device fixtures and time control
- [Using Hubitat CI in your repo](docs/using_in_your_repo.md) - Set up CI/CD, environment variables, and GitHub Actions

### Advanced
- [Publishing guide](docs/publishing.md) - How to publish updated versions of Hubitat_CI to Maven
- [Changelog](docs/changelog.md)

## Real-World Examples

See how integration tests are used in production Hubitat apps:

- **[Hubitat-Switch-Bindings](https://github.com/joelwetzel/Hubitat-Switch-Bindings)** - App integration tests for multi-way switch bindings
- **[Hubitat-Lockdown](https://github.com/joelwetzel/Hubitat-Lockdown)** - Complex multi-device coordination scenarios
- **[Hubitat-Auto-Shades](https://github.com/joelwetzel/Hubitat-Auto-Shades)** - Annotated tests showing integration testing best practices
- **[hubitat_ci_example](https://github.com/biocomp/hubitat_ci_example)** - Minimal examples and test patterns
