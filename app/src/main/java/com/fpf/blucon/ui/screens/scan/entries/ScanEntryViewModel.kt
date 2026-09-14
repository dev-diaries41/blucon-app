package com.fpf.blucon.ui.screens.scan.entries

import android.app.Application
import com.fpf.blucon.R
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
import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.bluetooth.scan.BTScanEntry
import com.fpf.blucon.data.paging.ScanEntriesPagingSource
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.storage.PrefsKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ScanEntryViewModel(
    application: Application,
    private val scanEntryRepository: ScanEntryRepository,
    private val sharedPrefs: SharedPreferences
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "ScanEntryViewModel"
    }

    private val _state = MutableStateFlow(ScanEntryScreenState())
    val state: StateFlow<ScanEntryScreenState> = _state

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

    val sortByOptions: List<Pair<String, SortBy>>
        get() = listOf(
            getApplication<Application>().getString(R.string.sort_date_asc_option) to SortBy.Date(descending = false),
            getApplication<Application>().getString(R.string.sort_date_desc_option) to SortBy.Date(descending = true),
            getApplication<Application>().getString(R.string.sort_name_asc_option) to SortBy.Name(descending = false),
            getApplication<Application>().getString(R.string.sort_name_desc_option) to SortBy.Name(descending = true),
            getApplication<Application>().getString(R.string.sort_rssi_asc_option) to SortBy.Rssi(descending = false),
            getApplication<Application>().getString(R.string.sort_rssi_desc_option) to SortBy.Rssi(descending = true),
        )

    init {
        load()
    }

    fun onAction(action: ScanEntryAction){
        when(action){
            is ScanEntryAction.SetSortBy -> setSortBy(action.sortBy)
        }
    }

    fun setScan(scan: BTScan) {
        viewModelScope.launch (Dispatchers.IO){
            val manufacturerCounts = scanEntryRepository.getManufacturerCounts(scan.id, limit = 6)
            val deviceCounts = scanEntryRepository.getDeviceNameCounts(scan.id, limit = 6)
            _state.update { it.copy(scan = scan, manufacturerCounts=manufacturerCounts.toMap(), deviceCounts=deviceCounts.toMap(), totalDevices = scan.size) }
        }
    }

    private fun load() {
        _state.update { it.copy(sortBy = getSortByPref()) }
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
}
