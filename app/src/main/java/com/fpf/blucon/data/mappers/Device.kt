package com.fpf.blucon.data.mappers

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.bluetooth.device.NewDeviceInfo
import com.fpf.blucon.data.devices.DeviceEntity
import com.fpf.blucon.data.devices.clusters.DeviceCollectionData


fun DeviceEntity.toDomain(): DeviceInfo = DeviceInfo(
    id=id,
    name=name
)

fun DeviceInfo.toDomain(): DeviceEntity = DeviceEntity(
    id=id,
    name=name
)

fun NewDeviceInfo.toEntity(): DeviceEntity = DeviceEntity(
    name=name
)

fun DeviceCollectionData.toDomain(): DeviceCollection = DeviceCollection(
    id = clusterId,
    name = label,
    size = size,
)

