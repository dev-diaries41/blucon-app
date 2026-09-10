package com.fpf.blucon.ui.screens.history

import android.app.Application
import com.fpf.blucon.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.data.paging.ScanHistoryPagingSource
import com.fpf.blucon.data.scans.ScanRepository
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.storage.PrefsKeys
import com.fpf.blucon.ui.utils.SelectionUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ScanHistoryViewModel(
    application: Application,
    private val scanRepository: ScanRepository,
    private val sharedPrefs: SharedPreferences
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "DevicesViewModel"
    }

    private val _state = MutableStateFlow(ScanHistoryState())
    val state: StateFlow<ScanHistoryState> = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    val scanHistory = _state
        .map { it.sortBy }
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
                        ScanHistoryPagingSource(
                            sortBy=sortBy,
                            scanRepository = scanRepository,
                        )
                    }
                ).flow

        }
        .cachedIn(viewModelScope)

//    private val _event = MutableSharedFlow<CollectionItemEvent>()
//    val event = _event.asSharedFlow()

    val sortByOptions: List<Pair<String, SortBy>>
        get() = listOf(
            getApplication<Application>().getString(R.string.sort_date_asc_option) to SortBy.Date(descending = false),
            getApplication<Application>().getString(R.string.sort_date_desc_option) to SortBy.Date(descending = true),
        )

    init {
        load()
    }

    fun onAction(action: ScanHistoryAction){
        when(action){
            is ScanHistoryAction.SetSelectAll -> setSelectAll(action.selectAll)
            is ScanHistoryAction.ToggleSelectionMode -> toggleSelectionMode()
            is ScanHistoryAction.ResetSelection -> resetSelection()
            is ScanHistoryAction.ClearSelection -> clearSelection()
            is ScanHistoryAction.SetSortBy -> setSortBy(action.sortBy)
            is ScanHistoryAction.Delete -> deleteScans()
            is ScanHistoryAction.Clear -> clearScans()
            is ScanHistoryAction.ToggleSelected -> toggleSelectedItem(action.item)
        }
    }

    private fun load() {
        _state.update { it.copy(sortBy = getSortByPref()) }
        setTotalItems()
    }

    private fun clearSelection() = _state.update{it.copy(selection = SelectionUtils.clearSelection(it.selection))}
    private fun resetSelection() = _state.update{it.copy(selection = SelectionUtils.resetSelection(it.selection))}
    private fun toggleSelectionMode() = _state.update { it.copy(selection = SelectionUtils.toggleSelectionMode(it.selection)) }

    private fun deleteScans(){
        viewModelScope.launch{
            val items = withContext(Dispatchers.IO) {
                getSelectedItems().toList()
            }
            scanRepository.deleteScans(items)
            resetSelection()
        }
    }

    private fun clearScans(){
        viewModelScope.launch{
            scanRepository.clearScans()
        }
    }
    private fun toggleSelectedItem(item: BTScan){
        _state.update {
            it.copy(selection = SelectionUtils.toggleSelectedItem(it.selection, item, it.totalScans))
        }
    }

    private fun setSelectAll(selectAll: Boolean) {
        _state.update { it.copy(selection = SelectionUtils.setSelectAll(it.selection, selectAll, it.totalScans))}

    }

    private suspend fun getSelectedItems(): Set<BTScan> = SelectionUtils.getSelectedItems(_state.value.selection){getAllScans()}

    private suspend fun getAllScans(): MutableSet<BTScan> {
        return scanRepository.getScans().toMutableSet()
    }

    private fun setSortBy(sortBy: SortBy) {
        _state.update { it.copy(sortBy = sortBy) }
        saveSortByPref(sortBy)
    }

    private fun saveSortByPref(sortBy: SortBy) {
        val option = sortByOptions.find { it.second == sortBy }?.second ?: sortByOptions.first().second
        sharedPrefs.edit {
            putString(PrefsKeys.SORT_BY_SCAN_HISTORY, option.toString())
        }
    }

    private fun getSortByPref(): SortBy {
        val sortByStr = sharedPrefs.getString(PrefsKeys.SORT_BY_SCAN_HISTORY, "") ?: ""
        return sortByOptions.find { it.second.toString() == sortByStr }?.second ?: SortBy.Date()
    }

    private fun setTotalItems(){
        viewModelScope.launch {
            val count = scanRepository.countScans()
            _state.update { it.copy(totalScans = count) }
        }
    }
}
