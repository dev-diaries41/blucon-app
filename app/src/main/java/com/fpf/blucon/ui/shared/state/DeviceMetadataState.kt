package com.fpf.blucon.ui.shared.state

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.metrics.CountMetric
import com.fpf.blucon.query.SortBy


data class DeviceMetadataState(
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val scanId: Long? = null,
    val topManufacturerCounts: List<CountMetric<Nothing>> = emptyList(),
    val topDeviceNameCounts:List<CountMetric<Nothing>> = emptyList(),
    val topCollectionCounts: List<CountMetric<DeviceCollection>> = emptyList(),
    val totalEntries: Int = 0
)