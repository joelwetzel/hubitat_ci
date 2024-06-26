package me.biocomp.hubitat_ci.util.integration

import me.biocomp.hubitat_ci.api.common_api.Log
import me.biocomp.hubitat_ci.device.HubitatDeviceSandbox
import me.biocomp.hubitat_ci.device.HubitatDeviceScript
import me.biocomp.hubitat_ci.util.integration.IntegrationDeviceExecutor
import me.biocomp.hubitat_ci.util.integration.IntegrationDeviceWrapper
import me.biocomp.hubitat_ci.util.integration.IntegrationScheduler
import me.biocomp.hubitat_ci.util.integration.TimeKeeper
import me.biocomp.hubitat_ci.validation.Flags
import me.biocomp.hubitat_ci.validation.NamedParametersValidator

import spock.lang.Specification

/**
 * The IntegrationDeviceSpecification is a Spock spec that sets up a mock Hub
 * environment for running integration tests on Hubitat device scripts.
 * It provisions the necessary objects and mocks to run the device script in a
 * controlled environment, and provides helper methods for setting up the
 * device settings and initializing the device script.
 * It lets the test writer focus on the device script logic, without having to
 * worry about the Hubitat_ci framework.
 * An example of use can be found in EnhancedVirtualPresenceSensorTests.groovy.
 */
abstract class IntegrationDeviceSpecification extends Specification {
    private HubitatDeviceSandbox sandbox
    protected HubitatDeviceScript deviceScript
    private scheduler = new IntegrationScheduler()

    protected device = Spy(IntegrationDeviceWrapper)

    protected log = Mock(Log)

    protected deviceState = [:]

    protected deviceExecutor = Spy(IntegrationDeviceExecutor, constructorArgs: [scheduler: scheduler]) {
        _*getLog() >> log
        _*getDevice() >> device
        _*getState() >> deviceState
    }

    /**
     * Set the device settings that will be used to run the device script, and initialize all
     * the necessary objects.
     * This method must be called in the overridden setup() method of the subclass specifications.
     */
    protected void initializeEnvironment(Map options) {
        validateAndUpdateOptions(options)

        sandbox = new HubitatDeviceSandbox(new File(options.deviceScriptFilename))
        deviceScript = sandbox.run(api: deviceExecutor, validationFlags: options.validationFlags, userSettingValues: options.userSettingValues)
        deviceExecutor.setSubscribingScript(deviceScript)
    }

    private static void validateAndUpdateOptions(Map options) {
        options.putIfAbsent('validationFlags', [])
        options.putIfAbsent('userSettingValues', [])
        optionsValidator.validate("Validating IntegrationAppSpecification options", options, EnumSet.noneOf(Flags))
    }

    private static final NamedParametersValidator optionsValidator = NamedParametersValidator.make {
        objParameter("deviceScriptFilename", required(), mustNotBeNull(), { v -> new Tuple2("String", v instanceof String)} )
        objParameter("validationFlags", notRequired(), mustNotBeNull(), { v -> new Tuple2("List<Flags>", v as List<Flags>) })
        objParameter("userSettingValues", notRequired(), mustNotBeNull(), { v -> new Tuple2("Map<String, Object>", v as Map<String, Object>) })
    }

    /**
     * Spock will run this base setup BEFORE running the overridden setup() in the subclass specifications.
     */
    def setup() {
        TimeKeeper.removeAllListeners()     // Ensure a clean slate for each test.
    }

    /**
     * Spock will run this base cleanup AFTER running the overridden cleanup() in the subclass specifications.
     */
    def cleanup() {
        TimeKeeper.removeAllListeners()     // The IntegrationScheduler is a TimeKeeper listener, so we want to unregister it after each test is complete.
    }

}