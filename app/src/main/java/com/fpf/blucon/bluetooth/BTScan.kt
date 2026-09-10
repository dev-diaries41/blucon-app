package com.fpf.blucon.bluetooth

data class BTScan(
    val id: Long,
    val latitude: Float,
    val longitude: Float,
    val timestamp: Long = System.currentTimeMillis(),
)

data class NewBTScan(
    val latitude: Float,
    val longitude: Float,
    val timestamp: Long = System.currentTimeMillis(),
)