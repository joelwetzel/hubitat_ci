package me.biocomp.hubitat_ci.api.device_api.zwave.commands.thermostatsetpointv2

trait ThermostatSetpointReport {
    abstract java.lang.String format()
    abstract java.lang.String getCMD()
    abstract java.lang.Short getCommandClassId()
    abstract java.lang.Short getCommandId()
    abstract java.util.List getPayload()
    abstract java.lang.Short getPrecision()
    abstract short getSETPOINT_TYPE_AUTO_CHANGEOVER()
    abstract short getSETPOINT_TYPE_AWAY_HEATING()
    abstract short getSETPOINT_TYPE_COOLING_1()
    abstract short getSETPOINT_TYPE_DRY_AIR()
    abstract short getSETPOINT_TYPE_ENERGY_SAVE_COOLING()
    abstract short getSETPOINT_TYPE_ENERGY_SAVE_HEATING()
    abstract short getSETPOINT_TYPE_FURNACE()
    abstract short getSETPOINT_TYPE_HEATING_1()
    abstract short getSETPOINT_TYPE_MOIST_AIR()
    abstract short getSETPOINT_TYPE_NOT_SUPPORTED()
    abstract short getSETPOINT_TYPE_NOT_SUPPORTED1()
    abstract short getSETPOINT_TYPE_NOT_SUPPORTED2()
    abstract short getSETPOINT_TYPE_NOT_SUPPORTED3()
    abstract short getSETPOINT_TYPE_NOT_SUPPORTED4()
    abstract java.lang.Short getScale()
    abstract java.math.BigDecimal getScaledValue()
    abstract java.lang.Short getSetpointType()
    abstract java.lang.Short getSize()
    abstract java.util.List getValue()
    abstract void setPrecision(java.lang.Short a)
    abstract void setScale(java.lang.Short a)
    abstract void setScaledValue(java.math.BigDecimal a)
    abstract void setSetpointType(java.lang.Short a)
    abstract void setSize(java.lang.Short a)
    abstract void setValue(java.util.List a)
}