package com.fpf.blucon.bluetooth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BTScan(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val size: Int,
    val timestamp: Long = System.currentTimeMillis(),
) : Parcelable

data class NewBTScan(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val size: Int = 0
)