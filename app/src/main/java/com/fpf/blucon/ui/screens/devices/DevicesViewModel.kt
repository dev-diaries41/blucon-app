package com.fpf.blucon.ui.screens.devices

import android.app.Application
import com.fpf.blucon.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.data.MetadataRepository
import com.fpf.blucon.data.paging.ScanEntriesPagingSource
import com.fpf.blucon.data.paging.ScanHistoryPagingSource
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.storage.PrefsKeys
import com.fpf.blucon.ui.utils.SelectionUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DevicesViewModel(
    application: Application,
    private val scanEntryRepository: ScanEntryRepository,
    private val sharedPrefs: SharedPreferences
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "DevicesViewModel"
    }

    private val _state = MutableStateFlow(DeviceScreenState())
    val state: StateFlow<DeviceScreenState> = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    val devices = _state
        .map { Pair(it.scan, it.sortBy,) }
        .distinctUntilChanged()
        .flatMapLatest { (scan, sortBy) ->
            if(scan?.id == null) return@flatMapLatest flowOf()

                Pager(
                    config = PagingConfig(
                        pageSize = 50,
                        initialLoadSize = 50,
                        prefetchDistance = 25,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        ScanEntriesPagingSource(
                            scanId=scan.id,
                            sortBy=sortBy,
                            scanEntryRepository = scanEntryRepository,
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

    fun onAction(action: DeviceAction){
        when(action){
            is DeviceAction.SetSortBy -> setSortBy(action.sortBy)
        }
    }

    fun setScan(scan: BTScan) = _state.update { it.copy(scan = scan) }

    private fun load() {
        _state.update { it.copy(sortBy = getSortByPref()) }
        setTotalItems()
    }

    private suspend fun getAllScans(): MutableSet<BTScanEntry> {
        return scanEntryRepository.getEntries().toMutableSet()
    }

    private fun setSortBy(sortBy: SortBy) {
        _state.update { it.copy(sortBy = sortBy) }
        saveSortByPref(sortBy)
    }

    private fun saveSortByPref(sortBy: SortBy) {
        val option = sortByOptions.find { it.second == sortBy }?.second ?: sortByOptions.first().second
        sharedPrefs.edit {
            putString(PrefsKeys.SORT_BY_DEVICES, option.toString())
        }
    }

    private fun getSortByPref(): SortBy {
        val sortByStr = sharedPrefs.getString(PrefsKeys.SORT_BY_DEVICES, "") ?: ""
        return sortByOptions.find { it.second.toString() == sortByStr }?.second ?: SortBy.Date()
    }

    private fun setTotalItems(){
        viewModelScope.launch {
            val scan = _state.value.scan?: return@launch
            _state.update { it.copy(totalDevices = scan.size) }
        }
    }
}
