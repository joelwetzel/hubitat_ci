package me.biocomp.hubitat_ci.integration

import me.biocomp.hubitat_ci.util.device_fixtures.SwitchFixtureFactory
import me.biocomp.hubitat_ci.util.integration.IntegrationAppSpecification
import me.biocomp.hubitat_ci.util.integration.TimeKeeper

/**
* This runs the same tests as SchedulerRecursionIntegrationTest, but using the IntegrationAppSpecification base class.
*/
class IntegrationAppSpecificationTest extends IntegrationAppSpecification {
    def switchFixture = SwitchFixtureFactory.create('s1')

    @Override
    def setup() {
        TimeKeeper.set(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:20:01"))    // This time is chosen to not be on a minute or 5 minute boundary

        super.initializeEnvironment(appScriptFilename: "Scripts/AppWithRecursiveSchedules.groovy",
                                    userSettingValues: [
                                        switches: [switchFixture],
                                        enableLogging: true
                                    ])

        switchFixture.initialize(appExecutor, [switch: "off"])
    }

    void "App initialize subscribes to events"() {
        when:
        appScript.initialize()

        then:
        // Expect that events are subscribed to
        1 * appExecutor.subscribe([switchFixture], 'switch.on', 'switchOnHandler')
        1 * appExecutor.subscribe([switchFixture], 'switch.off', 'switchOffHandler')
    }

    def "Handlers for scheduled jobs can go on to schedule their own jobs, without concurrency errors"() {
        given:
        appScript.initialize()

        and: "This will trigger a runInMillis callback to be scheduled for 50 milliseconds from now."
        switchFixture.on()

        when: "Wait long enough for the runInMillisHandler to be called the first time."
        TimeKeeper.advanceMillis(51)

        then:
        1 * log.debug("runInMillisHandler() called")

        when: "Wait long enough for the runInMillisHandler to be called the second time."
        TimeKeeper.advanceMillis(1001)

        then:
        1 * log.debug("runInMillisHandler() called")

        when: "Wait long enough for the runInMillisHandler to be called the third time."
        TimeKeeper.advanceMillis(1001)

        then:
        1 * log.debug("runInMillisHandler() called")
    }

    def "The recursive handler runs a maximum of 3 times, as per the app script logic"() {
        given:
        appScript.initialize()

        and: "This will trigger a runInMillis callback to be scheduled for 50 milliseconds from now."
        switchFixture.on()

        when: "Wait long enough for the runInMillisHandler to be called the first time."
        TimeKeeper.advanceMillis(51)

        then:
        1 * log.debug("runInMillisHandler() called")

        when: "Wait long enough for the runInMillisHandler to be called the second time."
        TimeKeeper.advanceMillis(1001)

        then:
        1 * log.debug("runInMillisHandler() called")

        when: "Wait long enough for the runInMillisHandler to be called the third time."
        TimeKeeper.advanceMillis(1001)

        then:
        1 * log.debug("runInMillisHandler() called")

        when: "Wait again, but it should not be called a fourth time."
        TimeKeeper.advanceMillis(1001)

        then:
        0 * log.debug("runInMillisHandler() called")
    }

    def "Periodic scheduling works in app scripts"() {
        given:
        appScript.initialize()

        and:
        switchFixture.off()

        when:
        TimeKeeper.advanceMinutes(1)

        then:
        1 * log.debug("Firing off scheduled event for tickTock")
        1 * log.debug("tickTock() called")

        when:
        TimeKeeper.advanceMinutes(1)

        then:
        1 * log.debug("Firing off scheduled event for tickTock")
        1 * log.debug("tickTock() called")

        when:
        TimeKeeper.advanceMinutes(5)

        then:
        5 * log.debug("Firing off scheduled event for tickTock")
        5 * log.debug("tickTock() called")
    }

}
