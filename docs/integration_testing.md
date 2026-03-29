# Integration Testing Guide

## Overview

Integration tests verify **end-to-end behavior** of your Hubitat apps and device drivers. Unlike validation tests (which check structure) or unit tests (which test individual methods with mocks), integration tests simulate how your code behaves in a real Hubitat environment.

**Integration tests vs Validation tests:**

| Validation Tests | Integration Tests |
|-----------------|-------------------|
| Automatic - no test code needed | You write test scenarios |
| Check structure and metadata | Test actual behavior |
| Run on every sandbox.run() | Use IntegrationAppSpecification |
| Fast validation | Realistic simulation |
| Catch structural errors | Catch logic errors |

See [How to Test](how_to_test.md) for details on validation and unit testing.

## When to Use Integration Tests

Use integration tests to verify:
- Time-based logic (schedules, delays, recurring events)
- Multi-device coordination
- Event handling and subscriptions
- State management across events
- Real-world scenarios ("when motion detected at night, turn on lights")

Use validation tests for:
- Ensuring definition/preferences are correct
- Checking API usage
- Structural validation

## Key Concepts

### Time Control with TimeKeeper

The `TimeKeeper` class gives you complete control over time in your tests. This allows you to:
- Test scheduled events without waiting
- Verify time-based logic
- Simulate the passage of hours, days, or weeks in seconds

```groovy
// Set the current time
TimeKeeper.set(Date.parse("yyyy-MM-dd HH:mm:ss", "2024-01-01 10:00:00"))

// Advance time by various increments
TimeKeeper.advanceMillis(500)
TimeKeeper.advanceSeconds(30)
TimeKeeper.advanceMinutes(5)
TimeKeeper.advanceHours(2)
TimeKeeper.advanceDays(1)

// Get the current time in tests
def currentTime = TimeKeeper.now()
```

### Integration Scheduler

The `IntegrationScheduler` automatically captures schedule requests from your app and triggers them when time advances:

- Supports all scheduling methods: `runIn`, `runInMillis`, `runOnce`, `schedule`, `runEvery1Minute`, etc.
- Evaluates cron expressions correctly
- Fires callbacks when time advances past their scheduled time
- Handles recurring schedules properly

### Device Fixtures

Device fixtures are pre-built mock devices that simulate realistic device behavior. Available fixtures include:

- **SwitchFixtureFactory**: Basic on/off switches
- **DimmerFixtureFactory**: Dimmable lights with level control
- **LockFixtureFactory**: Door locks with lock/unlock functionality
- **FanFixtureFactory**: Fans with speed control
- **WindowShadeFixtureFactory**: Window shades with position control
- **PresenceSensorFixtureFactory**: Presence sensors
- **LightSensorFixtureFactory**: Light level sensors

### Base Specification Classes

Two base classes simplify integration test setup:

- **IntegrationAppSpecification**: For testing apps
- **IntegrationDeviceSpecification**: For testing device drivers

## Testing Apps with IntegrationAppSpecification

### Basic Setup

Create a test that extends `IntegrationAppSpecification`:

```groovy
package me.biocomp.example

import me.biocomp.hubitat_ci.util.integration.IntegrationAppSpecification
import me.biocomp.hubitat_ci.util.device_fixtures.SwitchFixtureFactory

class MyAppIntegrationTest extends IntegrationAppSpecification {
    
    def switchFixture
    
    @Override
    def setup() {
        // Initialize the test environment with your app script
        super.initializeEnvironment(
            appScriptFilename: "MyApp.groovy",
            userSettingValues: [
                switches: [switchFixture],
                enableDebug: true
            ]
        )
        
        // Create device fixtures
        switchFixture = SwitchFixtureFactory.create('Living Room Switch')
        switchFixture.initialize(appExecutor, [switch: "off"])
        
        // Initialize your app
        appScript.initialize()
    }
    
    def "App responds to switch turning on"() {
        when:
            switchFixture.on()
        
        then:
            // Verify expected behavior
            1 * log.debug("Switch turned on")
    }
}
```

### Available Mocks and Objects

`IntegrationAppSpecification` provides these pre-configured objects:

- `appScript`: Your app script instance
- `appExecutor`: Spy on app executor methods
- `log`: Mock logger for verifying log messages
- `installedApp`: Mock installed app wrapper
- `appState`: Map for app state storage
- `appAtomicState`: Map for app atomic state storage

### Testing Scheduled Events

```groovy
def "Scheduled callback fires after time advances"() {
    given:
        appScript.initialize()
    
    when: "App schedules something to run in 5 minutes"
        appScript.scheduleCheck()
        TimeKeeper.advanceMinutes(5)
    
    then:
        1 * log.info("Scheduled check executed")
}

def "Recurring schedule fires multiple times"() {
    given:
        appScript.setupRecurringSchedule()
    
    when:
        TimeKeeper.advanceHours(1)
    
    then:
        1 * log.debug("Hourly check")
    
    when:
        TimeKeeper.advanceHours(1)
    
    then:
        1 * log.debug("Hourly check")
}
```

### Testing Event Subscriptions

```groovy
def "App subscribes to device events"() {
    when:
        appScript.initialize()
    
    then:
        1 * appExecutor.subscribe(switchFixture, 'switch.on', 'switchOnHandler')
        1 * appExecutor.subscribe(switchFixture, 'switch.off', 'switchOffHandler')
}

def "App responds to subscribed events"() {
    given:
        appScript.initialize()
    
    when:
        switchFixture.on()
    
    then:
        // Verify the handler was called
        1 * log.info("Switch is now on")
}
```

## Testing Device Drivers with IntegrationDeviceSpecification

### Basic Setup

Create a test that extends `IntegrationDeviceSpecification`:

```groovy
package me.biocomp.example

import me.biocomp.hubitat_ci.util.integration.IntegrationDeviceSpecification

class MyDeviceIntegrationTest extends IntegrationDeviceSpecification {
    
    @Override
    def setup() {
        super.initializeEnvironment(
            deviceScriptFilename: "MyDevice.groovy",
            userSettingValues: [
                switchState: "off",
                presence: "not present"
            ]
        )
    }
    
    def "Device can turn on"() {
        when:
            deviceScript.on()
        
        then:
            device.currentValue("switch") == "on"
    }
}
```

### Available Mocks and Objects

`IntegrationDeviceSpecification` provides:

- `deviceScript`: Your device driver instance
- `deviceExecutor`: Spy on device executor methods
- `log`: Mock logger
- `device`: Spy on device wrapper for checking current values
- `deviceState`: Map for device state storage

### Testing Device Commands and Attributes

```groovy
def "Device reports correct attributes"() {
    when:
        deviceScript.arrived()
    
    then:
        device.currentValue("switch") == "on"
        device.currentValue("presence") == "present"
}

def "Device transitions between states correctly"() {
    when:
        deviceScript.arrived()
    
    then:
        device.currentValue("presence") == "present"
    
    when:
        deviceScript.departed()
    
    then:
        device.currentValue("presence") == "not present"
}
```

### Testing Device Schedules

```groovy
def "Device schedules automatic refresh"() {
    when:
        deviceScript.configure()
        TimeKeeper.advanceMinutes(15)
    
    then:
        1 * log.debug("Refreshing device state")
}
```

## Working with Device Fixtures

### Creating and Initializing Fixtures

```groovy
def setup() {
    // Create a dimmer fixture
    def dimmer = DimmerFixtureFactory.create('Bedroom Dimmer')
    dimmer.initialize(appExecutor, [switch: "off", level: 0])
    
    // Create a lock fixture
    def lock = LockFixtureFactory.create('Front Door Lock')
    lock.initialize(appExecutor, [lock: "unlocked"])
}
```

### Interacting with Fixtures

```groovy
def "Dimmer responds to level commands"() {
    given:
        def dimmer = DimmerFixtureFactory.create('Dimmer')
        dimmer.initialize(appExecutor, [switch: "off", level: 0])
    
    when:
        dimmer.setLevel(50)
    
    then:
        dimmer.currentValue("level") == 50
        dimmer.currentValue("switch") == "on"
}

def "Lock reports correct state"() {
    given:
        def lock = LockFixtureFactory.create('Lock')
        lock.initialize(appExecutor, [lock: "unlocked"])
    
    when:
        lock.lock()
    
    then:
        lock.currentValue("lock") == "locked"
}
```

## Advanced Testing Scenarios

### Testing Complex Time-Based Behavior

```groovy
def "App handles day/night transitions"() {
    given:
        TimeKeeper.set(Date.parse("yyyy-MM-dd HH:mm:ss", "2024-01-01 08:00:00"))
        appScript.initialize()
    
    when: "Time advances to evening"
        TimeKeeper.advanceHours(12) // Now 8 PM
    
    then:
        1 * log.info("Switching to night mode")
    
    when: "Time advances to morning"
        TimeKeeper.advanceHours(12) // Now 8 AM next day
    
    then:
        1 * log.info("Switching to day mode")
}
```

### Testing Multiple Device Interactions

```groovy
def "App coordinates multiple devices"() {
    given:
        def switch1 = SwitchFixtureFactory.create('Switch 1')
        def switch2 = SwitchFixtureFactory.create('Switch 2')
        switch1.initialize(appExecutor, [switch: "off"])
        switch2.initialize(appExecutor, [switch: "off"])
        
        appScript.initialize()
    
    when:
        switch1.on()
    
    then:
        switch2.currentValue("switch") == "on" // App turned on switch2
}
```

### Testing Error Conditions

```groovy
def "App handles device failure gracefully"() {
    given:
        appScript.initialize()
        
    when: "Device becomes unavailable"
        deviceExecutor.sendEvent(switchFixture, [name: "status", value: "offline"])
    
    then:
        1 * log.warn("Device is offline")
        noExceptionThrown()
}
```

## Best Practices

1. **Use TimeKeeper consistently**: Always use `TimeKeeper.set()` to establish a known starting time
2. **Clean up between tests**: The base classes handle this, but be aware listeners are cleared between tests
3. **Verify subscriptions**: Check that your app subscribes to the right events
4. **Test edge cases**: Use time control to test midnight boundaries, DST transitions, etc.
5. **Keep tests focused**: Each test should verify one behavior
6. **Use meaningful fixture names**: Name fixtures after what they represent ("Living Room Light", not "switch1")

## Real-World Examples

These repositories show integration tests in production use. Each demonstrates different testing patterns and best practices.

### Hubitat-Switch-Bindings
**Repository:** [github.com/joelwetzel/Hubitat-Switch-Bindings](https://github.com/joelwetzel/Hubitat-Switch-Bindings)

**What it does:** Binds switches and dimmers together in 3-way, 4-way configurations

**Testing highlights:**
- Basic integration test setup with `IntegrationAppSpecification`
- Multiple switch fixtures interacting
- Testing master/slave relationships
- Dimmer level synchronization

**Example test structure:**
```groovy
class SwitchBindingsIntegrationTest extends IntegrationAppSpecification {
    def masterSwitch
    def slaveSwitches = []
    
    def setup() {
        masterSwitch = SwitchFixtureFactory.create('Master')
        slaveSwitches = [
            SwitchFixtureFactory.create('Slave 1'),
            SwitchFixtureFactory.create('Slave 2')
        ]
        
        super.initializeEnvironment(
            appScriptFilename: "Switch Bindings.groovy",
            userSettingValues: [
                master: masterSwitch,
                slaves: slaveSwitches
            ]
        )
        
        // Initialize fixtures
        masterSwitch.initialize(appExecutor, [switch: "off"])
        slaveSwitches.each { it.initialize(appExecutor, [switch: "off"]) }
        
        appScript.initialize()
    }
    
    def "When master switch turns on, all slaves turn on"() {
        when:
            masterSwitch.on()
        
        then:
            slaveSwitches.every { it.currentValue("switch") == "on" }
    }
}
```

**What you'll learn:**
- Setting up multiple device fixtures
- Testing device synchronization
- Basic integration test patterns

### Hubitat-Lockdown
**Repository:** [github.com/joelwetzel/Hubitat-Lockdown](https://github.com/joelwetzel/Hubitat-Lockdown)

**What it does:** Coordinates locks, switches, and modes for security scenarios

**Testing highlights:**
- Multiple device types (locks, switches, presence sensors)
- Mode changes and mode-dependent behavior
- Complex state machines
- Time-based mode switching

**Key patterns demonstrated:**
```groovy
def "Lockdown mode locks all doors and turns off switches"() {
    given:
        def lock = LockFixtureFactory.create('Front Door')
        def switches = [
            SwitchFixtureFactory.create('Living Room'),
            SwitchFixtureFactory.create('Kitchen')
        ]
        
        // Setup test environment
        super.initializeEnvironment(
            appScriptFilename: "Lockdown.groovy",
            userSettingValues: [
                locks: [lock],
                switches: switches
            ]
        )
        
        lock.initialize(appExecutor, [lock: "unlocked"])
        switches.each { it.initialize(appExecutor, [switch: "on"]) }
        appScript.initialize()
    
    when: "Lockdown mode is activated"
        appScript.activateLockdown()
    
    then: "All locks are locked"
        lock.currentValue("lock") == "locked"
    
    and: "All switches are off"
        switches.every { it.currentValue("switch") == "off" }
}
```

**What you'll learn:**
- Multi-device coordination
- Testing mode-based logic
- Complex state transitions
- Security-critical behavior testing

### Hubitat-Auto-Shades
**Repository:** [github.com/joelwetzel/Hubitat-Auto-Shades](https://github.com/joelwetzel/Hubitat-Auto-Shades)

**What it does:** Automatically controls window shades based on time, sun position, and temperature

**Testing highlights:**
- Well-commented test code showing best practices
- Time-of-day logic testing with `TimeKeeper`
- Window shade position fixtures
- Scheduled event testing
- Sunrise/sunset calculations

**Example showing time control:**
```groovy
def "Shades open at sunrise"() {
    given: "It's just before sunrise"
        TimeKeeper.set(Date.parse("yyyy-MM-dd HH:mm:ss", "2024-06-15 05:55:00"))
        
        def shade = WindowShadeFixtureFactory.create('Bedroom Shade')
        shade.initialize(appExecutor, [windowShade: "closed", position: 0])
        
        super.initializeEnvironment(
            appScriptFilename: "Auto Shades.groovy",
            userSettingValues: [
                shades: [shade],
                openAtSunrise: true
            ]
        )
        
        appScript.initialize()
    
    when: "Time advances to sunrise"
        TimeKeeper.advanceMinutes(10)  // Now 6:05 AM, after sunrise
    
    then: "Shade opens automatically"
        shade.currentValue("windowShade") == "open"
        shade.currentValue("position") == 100
}
```

**What you'll learn:**
- Comprehensive test documentation
- Time-based event testing
- Scheduled callback verification
- Best practices for readable tests

### Hubitat CI Example Repository
**Repository:** [github.com/biocomp/hubitat_ci_example](https://github.com/biocomp/hubitat_ci_example)

**What it does:** Minimal examples and test patterns for learning

**Contains:**
- `minimal/` - Simplest possible app and test
- `how_to_test/` - Mocking techniques and validation patterns
- Examples of both validation and integration tests

**What you'll learn:**
- Minimal project setup
- Different testing approaches
- Mocking patterns
- Preference and input validation

## Setting Up Integration Tests in Your Repository

For a complete guide on setting up testing in your own repository, including environment variables and GitHub Actions, see [Using Hubitat CI in Your Repo](using_in_your_repo.md).

Quick checklist:
- [ ] Add `hubitat_ci` dependency to `build.gradle`
- [ ] Set up GitHub token for package access
- [ ] Create test class extending `IntegrationAppSpecification`
- [ ] Initialize device fixtures in `setup()`
- [ ] Write tests using `TimeKeeper` for time control
- [ ] Set up GitHub Actions for CI (see [Using in Your Repo](using_in_your_repo.md))

## Comparison: Unit Tests vs Integration Tests

### Scheduled events not firing

Make sure you're advancing time enough for the event to fire:

```groovy
// This might not work if the event is scheduled for exactly 5 minutes
TimeKeeper.advanceMinutes(5)

// Better to advance slightly past the boundary
TimeKeeper.advanceSeconds(301) // 5 minutes + 1 second
```

### Events not triggering handlers

Verify your app subscribed correctly:

```groovy
def "Verify subscription"() {
    when:
        appScript.initialize()
    
    then:
        1 * appExecutor.subscribe(_, 'switch', _)
}
```

### Time not advancing in tests

Make sure you're using `TimeKeeper.now()` in your app, not `new Date()`. The framework automatically replaces `new Date()` with `TimeKeeper.now()` at compile time.

## Comparison: Unit Tests vs Integration Tests

| Aspect | Unit Tests | Integration Tests |
|--------|------------|-------------------|
| Focus | Individual methods | End-to-end behavior |
| Mocking | Heavy mocking | Realistic fixtures |
| Time | Instant | Can simulate hours/days |
| Complexity | Lower | Higher |
| Speed | Faster | Slower |
| Confidence | Method correctness | System correctness |

Use both! Unit tests for testing individual methods and edge cases, integration tests for verifying overall behavior.
