package com.fpf.blucon.data.scans

data class DeviceSummaryEntity(
    val deviceAddress: String,
    val rssi: Int,
    val lastSeen: Long,
    val scanCount: Int,
    val deviceName: String?,
    val manufacturerId: Int?,
)