package com.fpf.blucon.ui.shared.state

import com.fpf.blucon.query.SortBy


data class DeviceMetadataState(
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val scanId: Long? = null,
    val topManufacturerCounts: Map<String, Int> = mapOf(),
    val topDeviceNameCounts: Map<String, Int> = mapOf(),
    val topCollectionCounts: Map<String, Int> = mapOf(),
    val totalEntries: Int = 0
)