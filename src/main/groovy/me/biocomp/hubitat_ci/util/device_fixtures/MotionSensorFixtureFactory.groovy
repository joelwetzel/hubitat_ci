package me.biocomp.hubitat_ci.util.device_fixtures

import me.biocomp.hubitat_ci.app.preferences.DeviceInputValueFactory
import me.biocomp.hubitat_ci.validation.DefaultAndUserValues

import me.biocomp.hubitat_ci.capabilities.MotionSensor

/**
 * Factory for creating fixtures of motion sensor devices that behave like real motion sensors in Hubitat.
 * These devices can be used in testing of app scripts, by passing them as inputs.
 */
class MotionSensorFixtureFactory {
    /**
    * Constructs a new instance of a motion sensor device fixture.
    */
    static def create(String name) {
        def deviceInputValueFactory = new DeviceInputValueFactory([MotionSensor])

        def device = deviceInputValueFactory.makeInputObject(name)

        def deviceMetaClass = device.getMetaClass()

        // Calling initialize attaches behavior involving commands, state maintenance, and sending events.
        deviceMetaClass.initialize = { appExecutor, initialAttributeValues ->
            // The MotionSensor capability has no commands and only a single state attribute: motion.
            attributeValues = initialAttributeValues

            deviceMetaClass.activate = { ->
                attributeValues.motion = 'active'
                appExecutor.sendEvent(device, [name: "motion", value: 'active'])
            }

            deviceMetaClass.inactivate = { ->
                attributeValues.motion = 'inactive'
                appExecutor.sendEvent(device, [name: "motion", value: 'inactive'])
            }
        }

        return device
    }
}
