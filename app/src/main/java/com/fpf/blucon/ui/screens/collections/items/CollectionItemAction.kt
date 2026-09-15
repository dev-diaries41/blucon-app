package com.fpf.blucon.ui.screens.collections.items

import android.content.Context
import androidx.compose.ui.platform.Clipboard
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.query.SortBy

sealed interface CollectionItemAction {
    data class Move(val destinationCollection: DeviceCollection): CollectionItemAction
    data class CreateNewCollectionAndMove(val newName: String): CollectionItemAction
    data class ToggleSelectedMedia(val item: DeviceInfo): CollectionItemAction
//    data class Tag(val tag: String): CollectionItemAction
    data class SetSelectAll(val selectAll: Boolean): CollectionItemAction
    data class SetSortBy(val sortBy: SortBy): CollectionItemAction
//    data class Delete(val onDelete: (List<DeviceInfo>) -> Unit) : CollectionItemAction
//    data object ResetFilters: CollectionItemAction
//    data object RemoveTag : CollectionItemAction
    data object ToggleSelectionMode: CollectionItemAction
    data object ClearSelection: CollectionItemAction
    data object ResetSelection: CollectionItemAction
}