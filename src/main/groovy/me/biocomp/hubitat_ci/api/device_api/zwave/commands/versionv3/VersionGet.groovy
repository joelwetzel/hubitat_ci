package me.biocomp.hubitat_ci.api.device_api.zwave.commands.versionv3

trait VersionGet {
    abstract java.lang.String format()
    abstract java.lang.String getCMD()
    abstract java.lang.Short getCommandClassId()
    abstract java.lang.Short getCommandId()
    abstract java.util.List getPayload()
}
