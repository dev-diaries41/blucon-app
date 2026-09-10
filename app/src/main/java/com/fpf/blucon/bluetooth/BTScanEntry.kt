package com.fpf.blucon.bluetooth

data class BTScanEntry(
    val scanId: Long,
    val deviceAddress: String,
    val timestamp: Long,
    val rssi: Int,
    val manufacturerId: Int?,
    val deviceName: String?
)