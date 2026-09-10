package com.fpf.blucon.data.paging

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.ScanHistoryFilter
import com.fpf.blucon.data.scans.ScanRepository
import com.fpf.blucon.query.SortBy

class ScanHistoryPagingSource(
    filter: ScanHistoryFilter = ScanHistoryFilter(),
    sortBy: SortBy = SortBy.Date(),
    private val scanRepository: ScanRepository,
) : DataPagingSource<BTScan, ScanHistoryFilter>(filter=filter, sortBy=sortBy) {

    override suspend fun getItems(filter: ScanHistoryFilter, sortBy: SortBy, pageSize: Int, offset: Int): List<BTScan> = scanRepository.getScans(
        limit = pageSize + 1,
        offset = offset,
        descending = sortBy.descending,
    )
}