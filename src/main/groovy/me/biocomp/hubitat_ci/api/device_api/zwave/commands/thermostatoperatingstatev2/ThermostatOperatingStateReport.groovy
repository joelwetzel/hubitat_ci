package me.biocomp.hubitat_ci.api.device_api.zwave.commands.thermostatoperatingstatev2

trait ThermostatOperatingStateReport {
    abstract java.lang.String format()
    abstract java.lang.String getCMD()
    abstract java.lang.Short getCommandClassId()
    abstract java.lang.Short getCommandId()
    abstract java.lang.Short getOPERATING_STATE_2ND_STAGE_COOLING()
    abstract java.lang.Short getOPERATING_STATE_2ND_STAGE_HEATING()
    abstract java.lang.Short getOPERATING_STATE_AUX_HEATING()
    abstract java.lang.Short getOPERATING_STATE_COOLING()
    abstract java.lang.Short getOPERATING_STATE_FAN_ONLY()
    abstract java.lang.Short getOPERATING_STATE_HEATING()
    abstract java.lang.Short getOPERATING_STATE_IDLE()
    abstract java.lang.Short getOPERATING_STATE_PENDING_COOL()
    abstract java.lang.Short getOPERATING_STATE_PENDING_HEAT()
    abstract java.lang.Short getOPERATING_STATE_VENT_ECONOMIZER()
    abstract java.lang.Short getOperatingState()
    abstract java.util.List getPayload()
    abstract void setOPERATING_STATE_2ND_STAGE_COOLING(java.lang.Short a)
    abstract void setOPERATING_STATE_2ND_STAGE_HEATING(java.lang.Short a)
    abstract void setOPERATING_STATE_AUX_HEATING(java.lang.Short a)
    abstract void setOPERATING_STATE_COOLING(java.lang.Short a)
    abstract void setOPERATING_STATE_FAN_ONLY(java.lang.Short a)
    abstract void setOPERATING_STATE_HEATING(java.lang.Short a)
    abstract void setOPERATING_STATE_IDLE(java.lang.Short a)
    abstract void setOPERATING_STATE_PENDING_COOL(java.lang.Short a)
    abstract void setOPERATING_STATE_PENDING_HEAT(java.lang.Short a)
    abstract void setOPERATING_STATE_VENT_ECONOMIZER(java.lang.Short a)
    abstract void setOperatingState(java.lang.Short a)
}