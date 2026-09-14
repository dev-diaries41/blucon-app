package com.fpf.blucon.ui.screens.history

import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.query.SortBy


sealed interface ScanHistoryAction {
    data class ToggleSelected(val item: BTScan): ScanHistoryAction
    data class SetSelectAll(val selectAll: Boolean): ScanHistoryAction
    data class SetSortBy(val sortBy: SortBy): ScanHistoryAction
    data object Delete : ScanHistoryAction
    data object Clear : ScanHistoryAction
    data object ToggleSelectionMode: ScanHistoryAction
    data object ClearSelection: ScanHistoryAction
    data object ResetSelection: ScanHistoryAction
}