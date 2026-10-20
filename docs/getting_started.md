# Getting started

Hubitat CI provides two complementary types of testing:

1. **Validation Tests** - Automatically check script structure, metadata, and API usage
2. **Integration Tests** - Test actual behavior by simulating devices and events

This guide covers the basic setup. For more details, see:
- [How to Test](how_to_test.md) - Validation tests and mocking techniques
- [Integration Testing Guide](integration_testing.md) - End-to-end behavior testing
- [Using in Your Repo](using_in_your_repo.md) - Complete setup guide for your own repositories

## Quick Setup

1. [Install Gradle](https://gradle.org/install/) build system.
2. Look at a [minimal project sample (for app)](https://github.com/biocomp/hubitat_ci_example/tree/master/minimal) as a basis.
3. Copy the project structure and update `build.gradle` with your repository details.
4. Replace `appscript.groovy` with your own script.
5. Run `gradle build` from the same folder where `build.gradle` and your script are.

This will build and run the test, which will load and validate your script.

## What Gets Validated Automatically

When you create a sandbox and call `.run()`, Hubitat CI automatically validates:
- Definition blocks (name, namespace, author, capabilities)
- Preferences structure (pages, sections, inputs)
- Input types and options
- API method signatures
- Capability declarations

See [How to Test](how_to_test.md) for examples of what validation catches.

## Next Steps

- **For validation testing**: See [How to Test](how_to_test.md) for mocking and advanced validation
- **For integration testing**: See [Integration Testing Guide](integration_testing.md) for behavior testing
- **To set up your own repo**: See [Using in Your Repo](using_in_your_repo.md) for CI/CD and environment setup
