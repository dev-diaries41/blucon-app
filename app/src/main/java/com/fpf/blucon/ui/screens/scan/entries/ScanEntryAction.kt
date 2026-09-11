package com.fpf.blucon.ui.screens.scan.entries

import com.fpf.blucon.query.SortBy


sealed interface ScanEntryAction {
    data class SetSortBy(val sortBy: SortBy): ScanEntryAction

}