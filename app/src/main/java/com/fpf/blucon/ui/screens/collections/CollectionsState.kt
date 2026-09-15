package com.fpf.blucon.ui.screens.collections

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.ui.shared.state.SelectionState

data class CollectionsState(
    val showAllCollections: Boolean = false,
    val loading: Boolean = false,
    val collectToView: DeviceCollection? = null,
    val totalCollections: Int = 0,
    val selection: SelectionState<DeviceCollection> = SelectionState()
)