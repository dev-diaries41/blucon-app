package com.fpf.blucon.bluetooth

import kotlinx.serialization.Serializable

@Serializable
data class BTScanEntry(
    val scanId: Long,
    val deviceAddress: String,
    val timestamp: Long,
    val rssi: Int,
    val manufacturerId: Int?,
    val manufacturerName: String?,
    val deviceName: String?
)