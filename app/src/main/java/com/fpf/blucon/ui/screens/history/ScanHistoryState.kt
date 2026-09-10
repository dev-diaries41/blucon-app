package com.fpf.blucon.ui.screens.history

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.ui.shared.state.SelectionState


data class ScanHistoryState(
    val totalScans: Int = 0,
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val selection: SelectionState<BTScan> = SelectionState()
    )