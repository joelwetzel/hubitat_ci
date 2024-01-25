package me.biocomp.hubitat_ci.util

import groovy.time.*

/**
* TimeKeeperDate is just a Date that we can override the constructor for, without impacting
* the base Date constructor.
*/
class TimeKeeperDate extends Date {
}

/**
* The TimeKeeper is a class that intercepts and overrides the TimeKeeperDate constructor.
* This allows us to control the current time in tests, without having to build anything special into the app script itself.
* When parsing an app/device script, we'll replace references to "new Date()" with "new TimeKeeperDate()".
* As long as an app uses "new Date()" to get the current time, this class will control what time the app sees.
* Note that you must call install() before the app script is tested, and uninstall() after.
*/
class TimeKeeper {
    private Date internalDate = null

    TimeKeeper() {
        internalDate = new Date()
    }

    TimeKeeper(Date startingDate) {
        internalDate = startingDate
    }

    /**
    * Install the override of the default Date constructor
    */
    def install() {
        TimeKeeperDate.metaClass.constructor = { -> return this.now() }
    }

    /**
    * Uninstall the override of the default Date constructor, so that "new Date()"
    * will return the current time again.
    */
    def uninstall() {
        TimeKeeperDate.metaClass = null
    }

    def now() {
        return internalDate
    }

    def set(Date newDate) {
        internalDate = newDate
    }

    def advanceSeconds(int seconds) {
        internalDate = groovy.time.TimeCategory.plus(internalDate, new groovy.time.TimeDuration(0, 0, 0, seconds, 0))
    }

    def advanceMinutes(int minutes) {
        internalDate = groovy.time.TimeCategory.plus(internalDate, new groovy.time.TimeDuration(0, 0, minutes, 0, 0))
    }

    def advanceHours(int hours) {
        internalDate = groovy.time.TimeCategory.plus(internalDate, new groovy.time.TimeDuration(0, hours, 0, 0, 0))
    }

    def advanceDays(int days) {
        internalDate = groovy.time.TimeCategory.plus(internalDate, new groovy.time.TimeDuration(days, 0, 0, 0, 0))
    }
}