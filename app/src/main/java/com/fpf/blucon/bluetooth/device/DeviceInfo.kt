package com.fpf.blucon.bluetooth.device

// Only unique devices with name

data class DeviceInfo (
    val id: Long,
    val name: String
)

data class NewDeviceInfo (
    val name: String
)


