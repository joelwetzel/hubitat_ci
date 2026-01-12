# Changelog

## Version 0.23
- **NEW: Full Integration Testing Framework** - Added comprehensive integration testing support
  - `IntegrationAppSpecification` and `IntegrationDeviceSpecification` base classes for easy test setup
  - `IntegrationScheduler` for testing scheduled events and cron expressions
  - `TimeKeeper` class for controlling time in tests (advance seconds, minutes, hours, days)
  - `IntegrationAppExecutor` and `IntegrationDeviceExecutor` for realistic app/device execution
  - Device fixture factories: Switch, Dimmer, Lock, Fan, WindowShade, PresenceSensor, LightSensor
  - Support for testing event subscriptions and device interactions
  - Comprehensive [integration testing documentation](integration_testing.md)
- **BUG FIX**: Fixed variable name error in `IntegrationScheduler.runOnce(String, String, Map)` method (line 310)

## Version 0.9
- Added trace() method to Log interface

## Version 0.8
- Refreshed Hubitat APIs
- Auto compare APIs with dumped files with methods
- Fixed issue with page ordering (and reachability detection)
- More tests added

## Version 0.7
- Refreshed Hubitat APIs
- userSettingsValues support for HubitatDeviceScript + tests.
- Added warn log level
- Debugger detection (to avoid spurious property readings)
- Groovydoc is now embedded into the package.

## Version 0.5
- Support for enum values syntax with strings:
```groovy
input(... type: "enum", options: ["0": "Val1", "1": "Val2"], defaultValue: "1" ...)
```
- Bugfixes for capabilities.

## Version 0.4
Added partial validation of device's `parse()` method.