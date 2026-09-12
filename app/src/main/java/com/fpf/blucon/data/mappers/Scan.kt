package com.fpf.blucon.data.mappers

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.bluetooth.NewBTScan
import com.fpf.blucon.data.scans.ScanEntity
import com.fpf.blucon.data.scans.ScanEntryEntity
import com.fpf.blucon.data.scans.ScanWithCount


fun ScanWithCount.toDomain(): BTScan = BTScan(
    id = scan.id,
    timestamp=scan.timestamp,
    latitude = scan.latitude,
    longitude = scan.longitude,
    size=count
)

fun BTScan.toEntity(): ScanEntity = ScanEntity(
    id = id,
    timestamp=timestamp,
    latitude = latitude,
    longitude = longitude,
)

fun NewBTScan.toEntity(): ScanEntity = ScanEntity(
    timestamp=timestamp,
    latitude = latitude,
    longitude = longitude,
)

fun BTScanEntry.toEntity(): ScanEntryEntity = ScanEntryEntity(
    scanId = scanId,
    rssi=rssi,
    timestamp = timestamp,
    deviceAddress=deviceAddress,
    deviceName = deviceName,
    manufacturerId = manufacturerId
)

fun ScanEntryEntity.toDomain(): BTScanEntry = BTScanEntry(
    scanId = scanId,
    rssi=rssi,
    timestamp = timestamp,
    deviceAddress=deviceAddress,
    deviceName = deviceName,
    manufacturerId = manufacturerId,
    manufacturerName = null
)
