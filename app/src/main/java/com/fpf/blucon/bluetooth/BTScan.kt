package com.fpf.blucon.bluetooth

data class BTScan(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val size: Int,
    val timestamp: Long = System.currentTimeMillis(),
)

data class NewBTScan(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val size: Int = 0
)