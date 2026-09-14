package com.fpf.blucon.bluetooth.scan

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class BTScan(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val size: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val postcode: String? = null,
    ) : Parcelable

data class NewBTScan(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val size: Int = 0
)

fun NewBTScan.toScan(id: Long): BTScan = BTScan(
    id = id,
    timestamp=timestamp,
    latitude = latitude,
    longitude = longitude,
    size = size
)