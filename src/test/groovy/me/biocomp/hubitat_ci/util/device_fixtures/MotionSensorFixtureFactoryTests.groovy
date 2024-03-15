package me.biocomp.hubitat_ci.util.device_fixtures

import me.biocomp.hubitat_ci.api.app_api.AppExecutor
import me.biocomp.hubitat_ci.api.common_api.DeviceWrapper
import me.biocomp.hubitat_ci.api.common_api.Log
import me.biocomp.hubitat_ci.util.device_fixtures.MotionSensorFixtureFactory

import spock.lang.Specification

class MotionSensorFixtureFactoryTests extends Specification {
    def log = Mock(Log)

    def appExecutor = Mock(AppExecutor) {
        _*getLog() >> log
    }

    void "Motion sensor can report activity"() {
        given:
        def motionSensorFixture = MotionSensorFixtureFactory.create('n')
        motionSensorFixture.initialize(appExecutor, [motion: 'active'])

        expect:
        motionSensorFixture.currentValue('motion') == 'active'
    }

    void "Motion sensor reports changes in the measurement"() {
        given:
        def motionSensorFixture = MotionSensorFixtureFactory.create('n')
        motionSensorFixture.initialize(appExecutor, [motion: 'inactive'])

        when:
        motionSensorFixture.activate()

        then:
        1*appExecutor.sendEvent(motionSensorFixture, [name: "motion", value: 'active'])
        motionSensorFixture.currentValue('motion') == 'active'

        when:
        motionSensorFixture.inactivate()

        then:
        1*appExecutor.sendEvent(motionSensorFixture, [name: "motion", value: 'inactive'])
        motionSensorFixture.currentValue('motion') == 'inactive'
    }
}