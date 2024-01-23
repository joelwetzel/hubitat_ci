package me.biocomp.hubitat_ci

import groovy.time.*

import me.biocomp.hubitat_ci.util.TimeKeeper
import me.biocomp.hubitat_ci.util.TimeChangedEvent
import me.biocomp.hubitat_ci.util.TimeChangedListener

import spock.lang.Specification

class TimeKeeperTests extends Specification {
    def currentDate = new Date()

    def setup() {
    }

    void "Can make normal Date objects"() {
        when:
            def date = new Date()

        then:
            def diff = groovy.time.TimeCategory.minus(date, currentDate)
            diff.minutes == 0
            diff.hours == 0
            diff.days == 0
            diff.seconds < 1
    }

    void "Can use the TimeKeeper class to override default Date constructor"() {
        given:
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            timekeeper.install()

        when:
            def date = new Date()

        then:
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        cleanup:
            timekeeper.uninstall()
    }

    void "By default, TimeKeeper will start at current time"() {
        given:
            def timekeeper = new TimeKeeper()
            timekeeper.install()

        when:
            def date = new Date()

        then:
            date.toString() == currentDate.toString()

        cleanup:
            timekeeper.uninstall()
    }

    void "Can set the TimeKeeper after it's already constructed"() {
        given:
            def timekeeper = new TimeKeeper()
            timekeeper.install()

        when:
            def date = new Date()

        then:
            date.toString() == currentDate.toString()

        when:
            timekeeper.set(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            def date2 = new Date()

        then:
            date2 != currentDate
            date2.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        cleanup:
            timekeeper.uninstall()
    }

    void "Can advance the minutes of the TimeKeeper"() {
        given:
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            timekeeper.install()

        when:
            def date = new Date()

        then:
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        when:
            timekeeper.advanceMinutes(5)
            def date2 = new Date()

        then:
            date2 != currentDate
            date2.toString() == "Sun Aug 31 08:28:45 CDT 2014"

        cleanup:
            timekeeper.uninstall()
    }

    void "Can advance the seconds of the TimeKeeper"() {
        given:
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            timekeeper.install()

        when:
            def date = new Date()

        then:
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        when:
            timekeeper.advanceSeconds(5)
            def date2 = new Date()

        then:
            date2 != currentDate
            date2.toString() == "Sun Aug 31 08:23:50 CDT 2014"

        cleanup:
            timekeeper.uninstall()
    }

    void "Can advance the hours of the TimeKeeper"() {
        given:
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            timekeeper.install()

        when:
            def date = new Date()

        then:
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        when:
            timekeeper.advanceHours(5)
            def date2 = new Date()

        then:
            date2 != currentDate
            date2.toString() == "Sun Aug 31 13:23:45 CDT 2014"

        cleanup:
            timekeeper.uninstall()
    }

    void "Can advance the days of the TimeKeeper"() {
        given:
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            timekeeper.install()

        when:
            def date = new Date()

        then:
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        when:
            timekeeper.advanceDays(5)
            def date2 = new Date()

        then:
            date2 != currentDate
            date2.toString() == "Fri Sep 05 08:23:45 CDT 2014"

        cleanup:
            timekeeper.uninstall()
    }

    void "Uninstalling the TimeKeeper makes the Date class return current date/time again"() {
        given:
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            timekeeper.install()

        when:
            def date = new Date()

        then:
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        when:
            timekeeper.uninstall()
            def date2 = new Date()

        then:
            date2.toString() == currentDate.toString()
    }

    void "TimeKeeper will raise events to listeners"() {
        given: "A TimeKeeper with a listener registered"
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            def listener = Mock(TimeChangedListener)
            timekeeper.addListener(listener)
            timekeeper.install()

        when: "A date is created from the TimeKeeper"
            def date = new Date()

        then: "It should be the date we set"
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        when: "We advance time"
            timekeeper.advanceDays(5)

        then: "The listener should have received an event"
            1 * listener.timeChangedEventReceived(_) >> {
                TimeChangedEvent e  ->
                    e.source == timekeeper
                    e.oldTime.toString() == "Sun Aug 31 08:23:45 CDT 2014"
                    e.newTime.toString() == "Fri Sep 05 08:23:45 CDT 2014"
            }

        cleanup:
            timekeeper.uninstall()
    }

    void "Listeners can be unregistered from TimeKeeper"() {
        given: "A TimeKeeper with a registered listener"
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            def listener = Mock(TimeChangedListener)
            timekeeper.addListener(listener)
            timekeeper.install()

        when: "A date is created from the TimeKeeper"
            def date = new Date()

        then: "It should be the date we set"
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        when: "We unregister the listener"
            timekeeper.removeListener(listener)

        and: "We advance time"
            timekeeper.advanceDays(5)

        then: "The listener should not have received an event"
            0 * listener.timeChangedEventReceived(_)

        cleanup:
            timekeeper.uninstall()
    }

    void "TimeKeeper can have multiple listeners"() {
        given: "A TimeKeeper with two registered listeners"
            def timekeeper = new TimeKeeper(Date.parse("yyyy-MM-dd hh:mm:ss", "2014-08-31 8:23:45"))
            def listener1 = Mock(TimeChangedListener)
            def listener2 = Mock(TimeChangedListener)
            timekeeper.addListener(listener1)
            timekeeper.addListener(listener2)
            timekeeper.install()

        when: "A date is created from the TimeKeeper"
            def date = new Date()

        then: "It should be the date we set"
            date != currentDate
            date.toString() == "Sun Aug 31 08:23:45 CDT 2014"

        when: "We advance time"
            timekeeper.advanceDays(5)

        then: "Both listeners should have received an event"
            1 * listener1.timeChangedEventReceived(_) >> {
                TimeChangedEvent e  ->
                    e.source == timekeeper
                    e.oldTime.toString() == "Sun Aug 31 08:23:45 CDT 2014"
                    e.newTime.toString() == "Fri Sep 05 08:23:45 CDT 2014"
            }
            1 * listener2.timeChangedEventReceived(_) >> {
                TimeChangedEvent e  ->
                    e.source == timekeeper
                    e.oldTime.toString() == "Sun Aug 31 08:23:45 CDT 2014"
                    e.newTime.toString() == "Fri Sep 05 08:23:45 CDT 2014"
            }

        cleanup:
            timekeeper.uninstall()
    }
}
