package com.fpf.blucon.ui.screens.collections.items

import android.app.Application
import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.cluster.ClusterManager
import com.fpf.blucon.data.devices.DeviceRepository
import com.fpf.blucon.data.devices.clusters.DeviceClusterRepository
import com.fpf.blucon.data.paging.CollectionPagingSource
import com.fpf.blucon.data.paging.DevicePagingSource
import com.fpf.blucon.events.CollectionItemEvent
import com.fpf.blucon.events.CollectionItemEventType
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.storage.PrefsKeys
import com.fpf.blucon.ui.utils.SelectionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionItemsViewModel(
    application: Application,
    private val deviceClusterRepository: DeviceClusterRepository,
    private val clusterManager: ClusterManager,
    private val deviceRepository: DeviceRepository,
    private val sharedPrefs: SharedPreferences
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "CollectionItemsViewModel"
    }

    private val _state = MutableStateFlow(CollectionItemsState())
    val state: StateFlow<CollectionItemsState> = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionItems = _state
        .map { Pair( it.sortBy, it.collection) }
        .distinctUntilChanged()
        .flatMapLatest { ( sortBy, collection) ->

            if (collection?.id == null) {
                flowOf(PagingData.Companion.empty())
            } else {
                Pager(
                    config = PagingConfig(
                        pageSize = 50,
                        initialLoadSize = 50,
                        prefetchDistance = 25,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        DevicePagingSource(
                            clusterId=collection.id,
                            sortBy = sortBy,
                            deviceRepository = deviceRepository,
                        )
                    }
                ).flow
            }
        }
        .cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val collections = _state
        .map { Pair( it.sortBy, it.collection) }
        .distinctUntilChanged()
        .flatMapLatest { ( sortBy, collection) ->

            if (collection?.id == null) {
                flowOf(PagingData.Companion.empty())
            } else {
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
        }
        .cachedIn(viewModelScope)


    private val _event = MutableSharedFlow<CollectionItemEvent>()
    val event = _event.asSharedFlow()

    val sortByOptions: List<Pair<String, SortBy>>
        get() = listOf(
            getApplication<Application>().getString(R.string.sort_date_asc_option) to SortBy.Date(descending = false),
            getApplication<Application>().getString(R.string.sort_date_desc_option) to SortBy.Date(descending = true),
        )

    init {
        load()
    }

    fun onAction(action: CollectionItemAction){
        when(action){
            is CollectionItemAction.CreateNewCollectionAndMove -> createNewCollectionAndMove(action.newName)
            is CollectionItemAction.ToggleSelectedMedia -> toggleSelectedItem(action.item)
            is CollectionItemAction.Move -> moveItems(action.destinationCollection)
            is CollectionItemAction.SetSelectAll -> setSelectAll(action.selectAll)
            is CollectionItemAction.ToggleSelectionMode -> toggleSelectionMode()
            is CollectionItemAction.ResetSelection -> resetSelection()
            is CollectionItemAction.ClearSelection -> clearSelection()
            is CollectionItemAction.SetSortBy -> setSortBy(action.sortBy)
        }
    }

    private fun load() = _state.update { it.copy(sortBy = getSortByPref()) }
    private fun clearSelection() = _state.update{it.copy(selection = SelectionUtils.clearSelection(it.selection))}
    private fun resetSelection() = _state.update{it.copy(selection = SelectionUtils.resetSelection(it.selection))}
    private fun toggleSelectionMode() = _state.update { it.copy(selection = SelectionUtils.toggleSelectionMode(it.selection)) }
    
    private fun moveItems(newCollection: DeviceCollection){
        val currentCollection = _state.value.collection?: return
        _state.update { it.copy(loading = true) }

        viewModelScope.launch (Dispatchers.IO){
            try {
                val selectedItems = getSelectedItems()
                if (selectedItems.isEmpty()) return@launch
                clusterManager.moveItems(selectedItems.map{it.id}, newCollection.id, currentCollection.id)
                resetSelection()
                val message = if(selectedItems.size == 1 ) "Moved ${selectedItems.size} item" else "Moved ${selectedItems.size} items"
                _event.emit(CollectionItemEvent(CollectionItemEventType.MOVE, success = true, message = message))
            }catch (e: Exception){
                val message = "Error moving items"
                Log.e(TAG, "$message: ${e.message}")
                _event.emit(CollectionItemEvent(CollectionItemEventType.MOVE, success = false, message = message))
            }finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }


    private fun createNewCollectionAndMove( newCollectionName: String){
        val state = _state.value
        val currentCollection = state.collection?: return
        if(currentCollection.name == newCollectionName) return

        viewModelScope.launch (Dispatchers.IO) {
            try {
                val selectedItems = getSelectedItems()
                if (selectedItems.isEmpty()) return@launch
                clusterManager.createNewClusterAndMoveItems(selectedItems.map{it.id}, newCollectionName, currentCollection.id)
                resetSelection()
                val message = if(selectedItems.size == 1 ) "Moved ${selectedItems.size} item" else "Moved ${selectedItems.size} items"
                _event.emit(CollectionItemEvent(CollectionItemEventType.MOVE, success = true, message = message))
            }catch (_: SQLiteConstraintException){
                _event.emit(CollectionItemEvent(CollectionItemEventType.MOVE, success = false, message = "Collection already exists"))
            }catch (e: Exception){
                val message = "Error moving items"
                Log.e(TAG, "$message: ${e.message}")
                _event.emit(CollectionItemEvent(CollectionItemEventType.MOVE, success = false, message = message))            }
        }
    }


   private fun toggleSelectedItem(item: DeviceInfo){
       _state.update {
           it.copy(selection = SelectionUtils.toggleSelectedItem(it.selection, item, it.totalItems))
       }
   }

    private fun setSelectAll(selectAll: Boolean) {
        _state.update { it.copy(selection = SelectionUtils.setSelectAll(it.selection, selectAll, it.totalItems))}

    }

    private suspend fun getSelectedItems(): Set<DeviceInfo> = SelectionUtils.getSelectedItems(_state.value.selection){getAllItemInCollection()}

    private suspend fun getAllItemInCollection(): MutableSet<DeviceInfo> {
        val currentState = state.value
        val currentCollection = currentState.collection ?: return mutableSetOf()
        val itemsMatchingCluster = deviceRepository.getByCluster(currentCollection.id)
        return itemsMatchingCluster.toMutableSet()
    }


    fun setCollection(collection: DeviceCollection) {
        _state.update { it.copy(collection = collection) }
        setTotalItems()
    }

    private fun setSortBy(sortBy: SortBy) {
        _state.update { it.copy(sortBy = sortBy) }
        saveSortByPref(sortBy)
    }

    private fun saveSortByPref(sortBy: SortBy) {
        val option = sortByOptions.find { it.second == sortBy }?.second ?: sortByOptions.first().second
        sharedPrefs.edit {
            putString(PrefsKeys.SORT_BY_COLLECTION_ITEMS, option.toString())
        }
    }

    private fun getSortByPref(): SortBy {
        val sortByStr = sharedPrefs.getString(PrefsKeys.SORT_BY_COLLECTION_ITEMS, "") ?: ""
        return sortByOptions.find { it.second.toString() == sortByStr }?.second ?: SortBy.Date()
    }

    private fun setTotalItems(){
        val currentState = _state.value
        val collection = currentState.collection?: return
        _state.update { it.copy(totalItems = collection.size) }
    }
}