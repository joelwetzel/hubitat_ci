package me.biocomp.hubitat_ci.api.device_api.zwave.commands.schedulev1

trait CommandScheduleReport {
    abstract java.lang.String format()
    abstract java.lang.Short getActiveId()
    abstract java.lang.String getCMD()
    abstract java.lang.Short getCommandClassId()
    abstract java.lang.Short getCommandId()
    abstract java.lang.Integer getDurationByte()
    abstract java.lang.Short getDurationType()
    abstract java.lang.Short getNumberOfCmdToFollow()
    abstract java.util.List getPayload()
    abstract java.lang.Short getReportsToFollow()
    abstract java.lang.Boolean getRes51()
    abstract java.lang.Short getScheduleId()
    abstract java.lang.Short getStartDayOfMonth()
    abstract java.lang.Short getStartHour()
    abstract java.lang.Short getStartMinute()
    abstract java.lang.Short getStartMonth()
    abstract java.lang.Short getStartWeekday()
    abstract java.lang.Short getStartYear()
    abstract java.lang.Short getUserIdentifier()
    abstract void setActiveId(java.lang.Short a)
    abstract void setDurationByte(java.lang.Integer a)
    abstract void setDurationType(java.lang.Short a)
    abstract void setNumberOfCmdToFollow(java.lang.Short a)
    abstract void setReportsToFollow(java.lang.Short a)
    abstract void setRes51(java.lang.Boolean a)
    abstract void setScheduleId(java.lang.Short a)
    abstract void setStartDayOfMonth(java.lang.Short a)
    abstract void setStartHour(java.lang.Short a)
    abstract void setStartMinute(java.lang.Short a)
    abstract void setStartMonth(java.lang.Short a)
    abstract void setStartWeekday(java.lang.Short a)
    abstract void setStartYear(java.lang.Short a)
    abstract void setUserIdentifier(java.lang.Short a)
}