package com.fpf.blucon.ui.screens.scan.entries

import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.bluetooth.scan.BTScanEntry
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.ui.shared.state.SelectionState


data class ScanEntryScreenState(
    val scan: BTScan? = null,
    val totalDevices: Int = 0,
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val selection: SelectionState<BTScanEntry> = SelectionState(),
    val manufacturerCounts: Map<String, Int> = mapOf(),
    val deviceCounts: Map<String, Int> = mapOf()
)