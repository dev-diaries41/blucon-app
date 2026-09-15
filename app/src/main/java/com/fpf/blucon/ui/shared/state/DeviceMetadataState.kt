package com.fpf.blucon.ui.shared.state

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.query.SortBy


data class DeviceMetadataState(
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val scanId: Long? = null,
    val topManufacturerCounts: List<Triple<String, Nothing?, Int>> = emptyList(),
    val topDeviceNameCounts: List<Triple<String, Nothing?, Int>> = emptyList(),
    val topCollectionCounts: List<Triple<String, DeviceCollection, Int>> = emptyList(),
    val totalEntries: Int = 0
)