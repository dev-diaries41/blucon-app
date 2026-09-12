package com.fpf.blucon.ui.shared.state

import com.fpf.blucon.query.SortBy


data class DeviceMetadataState(
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val scanId: Long? = null,
)