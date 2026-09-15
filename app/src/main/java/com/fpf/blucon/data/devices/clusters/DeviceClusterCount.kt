package com.fpf.blucon.data.devices.clusters

import androidx.room.Embedded

data class DeviceCollectionCount (
    @Embedded val collection:  DeviceCollectionData,
    val count: Int
    )