package com.fpf.blucon.ui.screens.devices

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.ui.shared.state.SelectionState


data class DeviceScreenState(
    val scan: BTScan? = null,
    val totalDevices: Int = 0,
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val selection: SelectionState<BTScanEntry> = SelectionState()
    )