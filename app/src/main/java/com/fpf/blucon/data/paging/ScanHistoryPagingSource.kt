package com.fpf.blucon.data.paging

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.data.scans.ScanRepository
import com.fpf.blucon.query.SortBy

class ScanHistoryPagingSource(
    sortBy: SortBy = SortBy.Date(),
    private val scanRepository: ScanRepository,
) : DataPagingSource<BTScan, Nothing>(sortBy=sortBy) {

    override suspend fun getItems(sortBy: SortBy, pageSize: Int, offset: Int, filter: Nothing?): List<BTScan> = scanRepository.getScans(
        limit = pageSize + 1,
        offset = offset,
        descending = sortBy.descending,
    )
}