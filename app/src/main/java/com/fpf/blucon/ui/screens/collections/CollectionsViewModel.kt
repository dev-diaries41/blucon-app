package com.fpf.blucon.ui.screens.collections

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.cluster.ClusterManager
import com.fpf.blucon.events.CollectionEvent
import com.fpf.blucon.events.CollectionEventType
import com.fpf.blucon.ui.utils.SelectionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.paging.Pager
import androidx.paging.cachedIn
import com.fpf.blucon.data.devices.clusters.DeviceClusterRepository
import com.fpf.blucon.data.paging.CollectionPagingSource
import kotlinx.coroutines.flow.flatMapLatest

class CollectionsViewModel(
    application: Application,
    private val deviceClusterRepository: DeviceClusterRepository,
    private val clusterManager: ClusterManager,
    ) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "CollectionsViewModel"
    }

    private val _state = MutableStateFlow(CollectionsState())
    val state: StateFlow<CollectionsState> = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    val clusterCollections = _state
        .map {it.sortBy }
        .distinctUntilChanged()
        .flatMapLatest { sortBy ->
                Pager(
                    config = PagingConfig(
                        pageSize = 50,
                        initialLoadSize = 50,
                        prefetchDistance = 25,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        CollectionPagingSource(
                            sortBy = sortBy,
                            deviceClusterRepository = deviceClusterRepository,
                        )
                    }
                ).flow
            }
        .cachedIn(viewModelScope)

    private val _event = MutableSharedFlow<CollectionEvent>()
    val event = _event.asSharedFlow()

    fun onAction(action: CollectionAction){
        when(action){
            is CollectionAction.MergeCollections -> mergeCollections(action.primaryCollectionName, action.isNewMergedLabel)
            is CollectionAction.RenameCollection -> renameCollection(action.newName)
            is CollectionAction.ToggleSelectedCollection -> toggleSelectedCollection(action.collection)
            is CollectionAction.SetCollectionToView -> setCollectionToView(action.collection)
            is CollectionAction.DeleteCollections -> {}
            is CollectionAction.SetSelectAll -> setSelectAll(action.selectAll)
            is CollectionAction.ToggleSelectionMode -> toggleSelectionMode()
            is CollectionAction.ClearSelection -> clearSelection()
            is CollectionAction.ResetSelection -> resetSelection()
        }
    }

    init {
        load()
    }

    private fun load(){
        viewModelScope.launch (Dispatchers.IO){
            val count = deviceClusterRepository.count()
            _state.update { it.copy(totalCollections = count) }
        }
    }
    private fun clearSelection() = _state.update{it.copy(selection = SelectionUtils.clearSelection(it.selection))}
    private fun resetSelection() = _state.update{it.copy(selection = SelectionUtils.resetSelection(it.selection))}
    private fun toggleSelectionMode() = _state.update { it.copy(selection = SelectionUtils.toggleSelectionMode(it.selection)) }

    private fun renameCollection(newName: String){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val collection = getSelectedCollections().first()
                clusterManager.updateLabel(collection.id, newName)
                resetSelection()
                _event.emit(CollectionEvent(CollectionEventType.RENAME, success = true))
            } catch (_: SQLiteConstraintException){
                _event.emit(CollectionEvent(CollectionEventType.RENAME, success = false, message = "Collection already exists"))
            }
            catch (e: Exception){
                Log.e(TAG, "Error renaming collection: ${e.message}")
                _event.emit(CollectionEvent(CollectionEventType.RENAME, success = false, message = "Error renaming collection"))

            }
        }
    }

    private fun mergeCollections(primaryCollectionName: String, isNewMergedLabel: Boolean){
        _state.update { it.copy(loading = true) }

        viewModelScope.launch (Dispatchers.IO) {
            try {
                val selectedCollections = getSelectedCollections()
                if(selectedCollections.size < 2 ) return@launch
                var primaryCollection = selectedCollections.firstOrNull{it.name == primaryCollectionName}

                if(isNewMergedLabel) {
                    primaryCollection = selectedCollections.firstOrNull()
                    primaryCollection?.let { collection ->
                        clusterManager.updateLabel(collection.id, primaryCollectionName)
                    }
                }

                val newMergedCollection = primaryCollection?: error("No primary collection selected")
                val otherCollections = selectedCollections.filter { selectedCollection -> selectedCollection.id != newMergedCollection.id }
                clusterManager.mergeClusters(newMergedCollection.id, otherCollections.map { it.id })

                resetSelection()
                _event.emit(CollectionEvent(CollectionEventType.MERGE, success = true, "Merged ${selectedCollections.size} collections"))
            }
            catch (_: SQLiteConstraintException){
                _event.emit(CollectionEvent(CollectionEventType.MERGE, success = false, message = "Collection already exists"))
            }
            catch (e: Exception){
                val message = "Error merging collections"
                Log.e(TAG, "$message: ${e.message}")
                _event.emit(CollectionEvent(CollectionEventType.MERGE, success = false, message = message))
            }finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }


    private fun setCollectionToView(collection: DeviceCollection?) = _state.update { it.copy(collectToView = collection) }

    private fun toggleSelectedCollection(item: DeviceCollection){
        _state.update { it.copy(selection = SelectionUtils.toggleSelectedItem(it.selection, item, it.totalCollections)) }
    }

    private fun setSelectAll(selectAll: Boolean) {
        _state.update { it.copy(selection = SelectionUtils.setSelectAll(it.selection, selectAll, it.totalCollections))}
    }
    private suspend fun getSelectedCollections(): Set<DeviceCollection> = SelectionUtils.getSelectedItems(_state.value.selection){getAllCollections()}

    private suspend fun getAllCollections(): MutableSet<DeviceCollection> {
        return deviceClusterRepository.getCollections().toMutableSet()
    }

}
