package com.fpf.blucon.ui.screens.devices

import com.fpf.blucon.query.SortBy


sealed interface DeviceAction {
    data class SetSortBy(val sortBy: SortBy): DeviceAction

}