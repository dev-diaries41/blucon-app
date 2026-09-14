package com.fpf.blucon.data.mappers

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.bluetooth.device.NewDeviceInfo
import com.fpf.blucon.data.devices.DeviceNameEntity
import com.fpf.blucon.data.devices.clusters.DeviceCollectionData


fun DeviceNameEntity.toDomain(): DeviceInfo = DeviceInfo(
    id=id,
    name=name
)

fun DeviceInfo.toDomain(): DeviceNameEntity = DeviceNameEntity(
    id=id,
    name=name
)

fun NewDeviceInfo.toEntity(): DeviceNameEntity = DeviceNameEntity(
    name=name
)

fun DeviceCollectionData.toDomain(): DeviceCollection = DeviceCollection(
    id = clusterId,
    name = label,
    size = size,
)

