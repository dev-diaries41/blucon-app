package com.fpf.blucon.data.paging

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.query.SortBy

class CollectionCountsPagingSource(
    private val scanEntryRepository: ScanEntryRepository,
    private val scanId: Long?= null,
    sortBy: SortBy = SortBy.Name(),
) : DataPagingSource<Triple<String, DeviceCollection, Int>, Nothing>(
    filter = null,
    sortBy = sortBy,
) {
    override suspend fun getItems(sortBy: SortBy, pageSize: Int, offset: Int, filter: Nothing?): List<Triple<String, DeviceCollection, Int>> {
        val entries = scanEntryRepository.getClusterCounts(
            scanId=scanId,
            limit = pageSize + 1,
            offset = offset,
            descending = sortBy.descending
        )
        return entries
    }
}