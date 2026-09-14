package com.fpf.blucon.data.devices.clusters

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_cluster",
    indices = [
        Index(value = ["label"], unique = true),
    ])
data class DeviceClusterEntity (
    @PrimaryKey
    val clusterId: Long,
    val prototypeSize: Int,
    val meanSimilarity: Float = 0f,
    val stdSimilarity: Float = 0f,
    val label: String? = null,
    )