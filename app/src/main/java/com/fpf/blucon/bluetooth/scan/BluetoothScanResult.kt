package com.fpf.blucon.bluetooth.scan

import java.util.UUID

data class BluetoothScanResult(
    val deviceName: String?,
    val deviceAddress: String,
    val rssi: Int,
    val serviceUuids: List<UUID> = emptyList(),
    val manufacturerData: Map<Int, ByteArray> = emptyMap(),
    val serviceData: Map<UUID, ByteArray> = emptyMap(),
    val txPower: Int? = null,
    val rawAdvertisement: ByteArray? = null,
    val scanResponseData: ByteArray? = null
)