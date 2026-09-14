package com.fpf.blucon.data.mappers

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.cluster.ClusterCrossRef
import com.fpf.blucon.cluster.StoredClusterMetadata
import com.fpf.blucon.data.devices.clusters.ClusterCrossRefEntity
import com.fpf.blucon.data.devices.clusters.DeviceClusterEntity
import com.fpf.blucon.data.devices.clusters.DeviceCollectionData
import com.fpf.smartscansdk.core.cluster.ClusterMetadata


fun ClusterCrossRef.toEntity(): ClusterCrossRefEntity =
   ClusterCrossRefEntity(
        deviceId = deviceId,
        clusterId = clusterId,
    )

fun ClusterCrossRefEntity.toDomain(): ClusterCrossRef = ClusterCrossRef(
    deviceId = deviceId,
    clusterId = clusterId,
)

fun DeviceClusterEntity.toDomain(): StoredClusterMetadata = StoredClusterMetadata(
    clusterId = clusterId,
    label = label,
    meanSimilarity = meanSimilarity,
    stdSimilarity = stdSimilarity,
    prototypeSize = prototypeSize
)

fun StoredClusterMetadata.toEntity(): DeviceClusterEntity =
    DeviceClusterEntity(
        clusterId = clusterId,
        label = label,
        meanSimilarity = meanSimilarity,
        stdSimilarity = stdSimilarity,
        prototypeSize = prototypeSize
    )

fun StoredClusterMetadata.toIncrementalClusterMetadata(): ClusterMetadata = ClusterMetadata(
    label = label,
    meanSimilarity = meanSimilarity,
    stdSimilarity = stdSimilarity,
    prototypeSize = prototypeSize
)

fun DeviceCollectionData.toDomain(): DeviceCollection = DeviceCollection(
    id = clusterId,
    name = label,
    size = size,
)