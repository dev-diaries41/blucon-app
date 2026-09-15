package com.fpf.blucon.ui.screens.collections

import com.fpf.blucon.bluetooth.device.DeviceCollection


sealed interface CollectionAction {
    data class MergeCollections(val primaryCollectionName: String, val isNewMergedLabel: Boolean = false): CollectionAction
    data class RenameCollection(val newName: String): CollectionAction
    data class ToggleSelectedCollection(val collection: DeviceCollection): CollectionAction
    data class SetCollectionToView(val collection: DeviceCollection?): CollectionAction
    data class SetSelectAll(val selectAll: Boolean): CollectionAction
    data object DeleteCollections : CollectionAction
    data object ToggleSelectionMode: CollectionAction
    data object ClearSelection: CollectionAction
    data object ResetSelection: CollectionAction
}