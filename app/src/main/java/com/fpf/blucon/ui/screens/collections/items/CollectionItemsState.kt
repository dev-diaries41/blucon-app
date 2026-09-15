package com.fpf.blucon.ui.screens.collections.items

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.ui.shared.state.SelectionState

data class CollectionItemsState(
    val collection: DeviceCollection? = null,
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val selection: SelectionState<DeviceInfo> = SelectionState(),
    val totalItems: Int = 0
)