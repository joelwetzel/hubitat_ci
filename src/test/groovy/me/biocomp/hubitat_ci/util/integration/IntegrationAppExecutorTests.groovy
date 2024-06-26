package me.biocomp.hubitat_ci

import me.biocomp.hubitat_ci.util.integration.IntegrationAppExecutor
import me.biocomp.hubitat_ci.util.integration.DeviceEventArgs
import me.biocomp.hubitat_ci.util.integration.TimeKeeper
import me.biocomp.hubitat_ci.app.HubitatAppScript
import me.biocomp.hubitat_ci.api.common_api.DeviceWrapper

import spock.lang.Specification

/**
* We have to extend HubitatAppScript so that Spock Mock will know
* about the levelHandler/switchHandler methods that are called on it.
*/
abstract class EventHandlingAppScript extends HubitatAppScript {
    abstract void levelHandler(DeviceEventArgs args)
    abstract void switchHandler(DeviceEventArgs args)
}

class IntegrationAppExecutorTests extends Specification {
    def appExecutor = Spy(IntegrationAppExecutor)

    def appScript = Mock(EventHandlingAppScript)

    def setup() {
        appExecutor.setSubscribingScript(appScript)
    }

    def "IntegrationAppExecutor will forward a message when subscribed by attribute name"() {
        given:
        def device = Mock(DeviceWrapper) {
            _*getIdAsLong() >> 1
        }
        appExecutor.subscribe(device, 'level', 'levelHandler')

        when:
        appExecutor.sendEvent(device, [name: 'level', value: 50])

        then:
        1 * appScript.levelHandler(_) >> { DeviceEventArgs event ->
            assert event.deviceId == 1
            assert event.device == device
            assert event.name == 'level'
            assert event.value == 50
        }
    }

    def "IntegrationAppExecutor will forward a message when subscribed by attribute name and value"() {
        given:
        def device = Mock(DeviceWrapper) {
            _*getIdAsLong() >> 1
        }
        appExecutor.subscribe(device, 'switch.on', 'switchHandler')

        when:
        appExecutor.sendEvent(device, [name: 'switch', value: 'on'])

        then:
        1 * appScript.switchHandler(_) >> { DeviceEventArgs event ->
            assert event.deviceId == 1
            assert event.device == device
            assert event.name == 'switch'
            assert event.value == 'on'
        }
    }

    def "IntegrationAppExecutor does not forward a message when subscribed by attribute name and value, and value does not match"() {
        given:
        def device = Mock(DeviceWrapper) {
            _*getIdAsLong() >> 1
        }
        appExecutor.subscribe(device, 'switch.off', 'switchHandler')

        when:
        appExecutor.sendEvent(device, [name: 'switch', value: 'on'])

        then:
        0 * appScript.switchHandler(_) >> { DeviceEventArgs event ->
            assert event.deviceId == 1
            assert event.device == device
            assert event.name == 'switch'
            assert event.value == 'on'
        }
    }

    def "DeviceEventArgs are for the correct device"() {
        given:
        def device1 = Mock(DeviceWrapper) {
            _*getIdAsLong() >> 1
        }
        def device2 = Mock(DeviceWrapper) {
            _*getIdAsLong() >> 2
        }
        appExecutor.subscribe(device1, 'level', 'levelHandler')
        appExecutor.subscribe(device2, 'level', 'levelHandler')

        when:
        appExecutor.sendEvent(device2, [name: 'level', value: 55])

        then:
        1 * appScript.levelHandler(_) >> { DeviceEventArgs event ->
            assert event.deviceId == 2
            assert event.device == device2
            assert event.name == 'level'
            assert event.value == 55
        }
    }

    def "DeviceEventArgs are for the correct device, when subscribed in a list"() {
        given:
        def device1 = Mock(DeviceWrapper) {
            _*getIdAsLong() >> 1
        }
        def device2 = Mock(DeviceWrapper) {
            _*getIdAsLong() >> 2
        }
        appExecutor.subscribe([device1, device2], 'level', 'levelHandler')

        when:
        appExecutor.sendEvent(device2, [name: 'level', value: 55])

        then:
        1 * appScript.levelHandler(_) >> { DeviceEventArgs event ->
            assert event.deviceId == 2
            assert event.device == device2
            assert event.name == 'level'
            assert event.value == 55
        }
    }

    def "IntegrationAppExecutor ignores unsubscribed events"() {
        given:
        def device = Mock(DeviceWrapper) {
            _*getIdAsLong() >> 1
        }
        appExecutor.subscribe(device, 'level', 'levelHandler')

        when:
        appExecutor.sendEvent(device, [name: 'switch', value: 'on'])

        then:
        0 * appScript.levelHandler(_)
    }

    def "Dates are parsed correctly by toDateTime method" () {
        given:
        def dateString = "2020-11-02T14:32:17+0000"

        when:
        def date = appExecutor.toDateTime(dateString)

        then:
        // This is the format that Hubitat hub uses when it converts dates to strings
        // (as it does when storing a date in app or device state)
        date.format("yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getTimeZone('UTC')) == dateString
    }

    def "now() integrates with the TimeKeeper class to return simulated times"() {
        given:
        def trueNowMillis = (new Date()).getTime()

        def simulatedDate = Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45")
        TimeKeeper.set(simulatedDate)

        when:
        def appExecutorNowMillis = appExecutor.now()

        then:
        appExecutorNowMillis == simulatedDate.getTime()
        appExecutorNowMillis != trueNowMillis
    }
}