# Integration Testing Guide

## Overview

The integration testing framework allows you to write end-to-end tests for your Hubitat apps and device drivers. Unlike unit tests that focus on individual methods with mocks, integration tests verify that your entire app or driver behaves correctly when interacting with devices, schedules, and events.

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

For complete examples of integration tests in action, see:

- [Hubitat-Switch-Bindings](https://github.com/joelwetzel/Hubitat-Switch-Bindings) - Examples of app integration tests
- [Hubitat-Lockdown](https://github.com/joelwetzel/Hubitat-Lockdown) - Complex multi-device scenarios
- [Hubitat-Auto-Shades](https://github.com/joelwetzel/Hubitat-Auto-Shades) - Annotated integration tests showing best practices

## Troubleshooting

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
