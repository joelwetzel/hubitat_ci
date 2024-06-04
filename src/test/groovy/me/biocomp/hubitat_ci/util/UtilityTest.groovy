package me.biocomp.hubitat_ci.util

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import me.biocomp.hubitat_ci.util.integration.TimeKeeper

class UtilityTest extends
        Specification
{
    @Shared
    Date currentDate = new Date()

    @Shared
    def myTimeZone = new GregorianCalendar().getTimeZone()

    def "timeToday can parse"() {
        given:
        TimeKeeper.reset()
        def now = TimeKeeper.now()
        def timeString = "2020-02-24T02:00:00.000-0600"

        when:
        def timeToday = Utility.timeToday(timeString)

        then:
        timeToday.hours == 2
        timeToday.minutes == 0
        timeToday.seconds == 0
        timeToday.year == now.year
        timeToday.month == now.month
        timeToday.day == now.day
    }

    @Unroll
    def "timeToday properly appends time (#timeString)"(String timeString, TimeZone tz, int expectedHr, int expectedMin,
                                          int expectedSec) {
        given:
            TimeKeeper.reset()

        expect:
            Date today = Utility.timeToday(timeString, tz)
            today.hours == expectedHr
            today.minutes == expectedMin
            today.seconds == expectedSec

            today.year + 1900 > 2017            // Years in the java.util.Date object are counted starting at year 1900
            today.month >= 0
            today.month < 12
            today.day >= 0 // Shouldn't really be == 0, but somehow it happens in VSTS.
            today.day <= 31

        where:
            timeString             | tz         || expectedHr || expectedMin || expectedSec
            "00:00"                | myTimeZone || 0          || 0           || 0
            "10:20"                | myTimeZone || 10         || 20          || 0
            "2015-08-18T11:22:33Z" | myTimeZone || 11         || 22          || 33
    }

    @Unroll
    def "Map parsing tests"(String input, Map expectedResult) {
        expect:
            Utility.stringToMap(input) == expectedResult

        where:
            input         | expectedResult
            ""            | [:]
            "a:b"         | ["a": "b"]
            "a: b, c:  d" | [a: "b", c: "d"]
    }

//    @Unroll
//    def "TimeOfDayInBetween test with no timezone"(Date start, Date end, Date toCheck, boolean isBetween) {
//        expect:
//            Utility.timeOfDayIsBetween(start, end, toCheck, new GregorianCalendar().timeZone) == isBetween
//
//        where:
//            start           | end             | toCheck     | isBetween
//            currentDate - 1 | currentDate + 1 | currentDate | true
//            currentDate + 1 | currentDate + 2 | currentDate | false
//            currentDate - 2 | currentDate - 1 | currentDate | false
//    }


    @Unroll
    def "Parsing time strings successfully"(String input, int hours, int minutes, int seconds) {
        expect:
            Utility.parseTimeString(input) == [hours: hours, minutes: minutes, seconds: seconds]

        where:
            input                    || hours | minutes | seconds
            "10:34"                  || 10    | 34      | 0
            "00:00"                  || 0     | 0       | 0
            "19:00"                  || 19    | 0       | 0
            "2015-08-18T00:00+01"    || 0     | 00      | 0 // Not using zone offset.
            "2015-08-18T10:20+01"    || 10    | 20      | 0 // Not using zone offset.
            "2015-08-18T19:23Z"      || 19    | 23      | 0 // Not using zone offset.
            "2015-08-18T11:22+01:02" || 11    | 22      | 0 // Not using zone offset.
            "2015-08-18T11:22:33Z"   || 11    | 22      | 33
    }
}
