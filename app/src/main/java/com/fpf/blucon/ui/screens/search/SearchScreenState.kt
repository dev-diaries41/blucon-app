package com.fpf.blucon.ui.screens.search

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.ui.shared.state.SelectionState


data class SearchScreenState(
    val totalResults: Int = 0,
    val sortBy: SortBy = SortBy.Date(),
    val loading: Boolean = false,
    val selection: SelectionState<BTScanEntry> = SelectionState(),
    val query: String? = null,
    val manufacturerCounts: Map<String, Int> = mapOf()
)