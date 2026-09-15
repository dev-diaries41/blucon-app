package com.fpf.blucon.data.paging

import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.metrics.CountMetric
import com.fpf.blucon.query.SortBy

class DeviceCountsPagingSource(
    private val scanEntryRepository: ScanEntryRepository,
    private val scanId: Long?= null,
    sortBy: SortBy = SortBy.Date(),
) : DataPagingSource<CountMetric<Nothing>, Nothing>(
    filter = null,
    sortBy = sortBy,
) {
    override suspend fun getItems(sortBy: SortBy, pageSize: Int, offset: Int, filter: Nothing?): List<CountMetric<Nothing>> {
        val entries = scanEntryRepository.getDeviceNameCounts(
            scanId=scanId,
            limit = pageSize + 1,
            offset = offset,
            descending = sortBy.descending
        )
        return entries
    }
}