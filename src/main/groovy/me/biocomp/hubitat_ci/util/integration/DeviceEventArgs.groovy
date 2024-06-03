package me.biocomp.hubitat_ci.util.integration

import me.biocomp.hubitat_ci.api.common_api.DeviceWrapper

/**
* Class for sending events to handlers in app scripts.  It replicates
* the key parts of the events that the Hubitat framework passes to app scripts.
* Note: There is an Event trait in Event.groovy, that includes ALL the properties
* and methods that Hubitat provides.  However, very few of those props/methods
* are typically used in app scripts, so this class is a slimmed down version.
*/
class DeviceEventArgs {
    DeviceWrapper device
    String name
    String type
    Object value

    DeviceEventArgs(DeviceWrapper device, String name, String type, Object value) {
        this.device = device
        this.name = name
        this.type = type
        this.value = value
    }

    Long getDeviceId() {
        return device.getIdAsLong()
    }

    String getDisplayName() {
        return device.getDisplayName()
    }
}