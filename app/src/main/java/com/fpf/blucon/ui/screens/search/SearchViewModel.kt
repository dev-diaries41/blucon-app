package com.fpf.blucon.ui.screens.search

import android.app.Application
import com.fpf.blucon.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import androidx.compose.foundation.text.input.TextFieldState
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.fpf.blucon.data.MetadataRepository
import com.fpf.blucon.data.paging.ScanEntriesPagingSource
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.query.SortBy
import com.fpf.blucon.storage.PrefsKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SearchViewModel(
    application: Application,
    private val scanEntryRepository: ScanEntryRepository,
    private val metadataRepository: MetadataRepository,
    private val sharedPrefs: SharedPreferences
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "SearchViewModel"
    }

    val searchFieldState: TextFieldState = TextFieldState()

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults = _state
        .map { Pair(it.query, it.sortBy) }
        .distinctUntilChanged()
        .flatMapLatest { (query, sortBy) ->
            if(query == null) return@flatMapLatest flowOf()

                Pager(
                    config = PagingConfig(
                        pageSize = 50,
                        initialLoadSize = 50,
                        prefetchDistance = 25,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        ScanEntriesPagingSource(
                            query=query,
                            sortBy=sortBy,
                            scanEntryRepository = scanEntryRepository,
                            metadataRepository = metadataRepository,
                            isSearching = true
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

    init {
        load()
    }

    fun onAction(action: SearchAction){
        when(action){
            is SearchAction.SetSortBy -> setSortBy(action.sortBy)
            is SearchAction.Search -> search(searchFieldState.text.toString())
        }
    }

    private fun search(query: String){
        _state.update { it.copy(query=query) }
    }
    private fun load() {
        _state.update { it.copy(sortBy = getSortByPref()) }
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
            val totalResult = 0  // TODO: add method to get count
            _state.update { it.copy(totalResults = totalResult) }
        }
    }
}
