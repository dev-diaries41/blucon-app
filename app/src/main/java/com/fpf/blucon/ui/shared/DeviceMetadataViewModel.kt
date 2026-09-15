package com.fpf.blucon.ui.shared

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.cluster.ClusterManager
import com.fpf.blucon.data.devices.clusters.DeviceClusterRepository
import com.fpf.blucon.data.paging.CompanyCountsPagingSource
import com.fpf.blucon.data.paging.DeviceCountsPagingSource
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.ui.shared.state.DeviceMetadataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DeviceMetadataViewModel(
    application: Application,
    private val scanEntryRepository: ScanEntryRepository,
    private val deviceClusterRepository: DeviceClusterRepository
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


    fun setOverviewInfo(scan: BTScan? = null) {
        viewModelScope.launch {
            val manufacturerCounts = scanEntryRepository.getManufacturerCounts(limit = 6, scanId = scan?.id)
            val deviceNameCounts = scanEntryRepository.getDeviceNameCounts(limit = 6, scanId = scan?.id)
            val topCollectionCounts = scanEntryRepository.getClusterCounts(limit = 6, scanId = scan?.id)
            val totalEntries = scan?.size ?: scanEntryRepository.countEntries()
            _state.update { it.copy( scanId = scan?.id, topManufacturerCounts = manufacturerCounts, topDeviceNameCounts=deviceNameCounts, totalEntries=totalEntries, topCollectionCounts=topCollectionCounts) }
        }
    }
}
