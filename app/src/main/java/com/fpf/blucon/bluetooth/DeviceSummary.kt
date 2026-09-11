package com.fpf.blucon.bluetooth

data class DeviceSummary(
    val deviceAddress: String,
    val deviceName: String?,
    val manufacturerId: Int?,
    val manufacturerName: String?,
    val rssi: Int,
    val lastSeen: Long,
    val scanCount: Int
)