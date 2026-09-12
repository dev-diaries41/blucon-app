package com.fpf.blucon.ui.shared

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
import com.fpf.blucon.data.paging.CompanyCountsPagingSource
import com.fpf.blucon.data.paging.DeviceCountsPagingSource
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.storage.PrefsKeys
import com.fpf.blucon.ui.shared.state.DeviceMetadataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DeviceMetadataViewModel(
    application: Application,
    private val scanEntryRepository: ScanEntryRepository,
    private val sharedPrefs: SharedPreferences
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "DeviceMetadataViewModel"
    }


    private val _state = MutableStateFlow(DeviceMetadataState())
    val state: StateFlow<DeviceMetadataState> = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    val companyCounts = _state
        .map { Pair(it.scanId, it.sortBy) }
        .distinctUntilChanged()
        .flatMapLatest { (scanId, sortBy) ->
            Pager(
                config = PagingConfig(
                    pageSize = 50,
                    initialLoadSize = 50,
                    prefetchDistance = 25,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    CompanyCountsPagingSource(
                        scanId = scanId,
                        sortBy=sortBy,
                        scanEntryRepository = scanEntryRepository,
                    )
                }
            ).flow

        }
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val deviceNameCounts = _state
        .map { Pair(it.scanId, it.sortBy) }
        .distinctUntilChanged()
        .flatMapLatest { (scanId, sortBy) ->
            Pager(
                config = PagingConfig(
                    pageSize = 50,
                    initialLoadSize = 50,
                    prefetchDistance = 25,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    DeviceCountsPagingSource(
                        scanId = scanId,
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
        )

    fun setScanId(scanId: Long){
        _state.update { it.copy(scanId = scanId) }
    }

//    init {
//        load()
//    }

//    private fun load() {
//        viewModelScope.launch {
//            _state.update { it.copy(sortBy = getSortByPref()) }
//        }
//    }

//    private fun setSortBy(sortBy: SortBy) {
//        _state.update { it.copy(sortBy = sortBy) }
//        saveSortByPref(sortBy)
//    }
//
//    private fun saveSortByPref(sortBy: SortBy) {
//        val option = sortByOptions.find { it.second == sortBy }?.second ?: sortByOptions.first().second
//        sharedPrefs.edit {
//            putString(PrefsKeys.SORT_BY_DEVICES, option.toString())
//        }
//    }
//
//    private fun getSortByPref(): SortBy {
//        val sortByStr = sharedPrefs.getString(PrefsKeys.SORT_BY_DEVICES, "") ?: ""
//        return sortByOptions.find { it.second.toString() == sortByStr }?.second ?: SortBy.Date()
//    }
}
