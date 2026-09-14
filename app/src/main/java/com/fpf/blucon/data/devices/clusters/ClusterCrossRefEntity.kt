package com.fpf.blucon.data.devices.clusters

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.fpf.blucon.data.devices.DeviceNameEntity

@Entity(
    tableName = "device_cluster_crossref",
    primaryKeys = ["deviceId", "clusterId"],
    foreignKeys = [
        ForeignKey(
            entity = DeviceClusterEntity::class,
            parentColumns = ["clusterId"],
            childColumns = ["clusterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DeviceNameEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["clusterId"])
    ]
)
data class ClusterCrossRefEntity(
    val deviceId: Long,
    val clusterId: Long
)