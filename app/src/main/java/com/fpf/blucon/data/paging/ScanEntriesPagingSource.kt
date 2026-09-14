package com.fpf.blucon.data.paging

import com.fpf.blucon.bluetooth.scan.BTScanEntry
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.query.SortBy

class ScanEntriesPagingSource(
    private val scanEntryRepository: ScanEntryRepository,
    private val scanId: Long?= null,
    sortBy: SortBy = SortBy.Date(),
) : DataPagingSource<BTScanEntry, Nothing>(
    filter = null,
    sortBy = sortBy,
) {
    override suspend fun getItems(sortBy: SortBy, pageSize: Int, offset: Int, filter: Nothing?): List<BTScanEntry> {
        val entries= when(sortBy){
            is SortBy.Date -> scanEntryRepository.getEntries(
                scanId=scanId,
                limit = pageSize + 1,
                offset = offset,
                descending = sortBy.descending
            )

            is SortBy.Name -> scanEntryRepository.getEntriesByName(
                scanId=scanId,
                limit = pageSize + 1,
                offset = offset,
                descending = sortBy.descending
            )
            is SortBy.Rssi -> scanEntryRepository.getEntriesByRssi(
                scanId=scanId,
                limit = pageSize + 1,
                offset = offset,
                descending = sortBy.descending
            )
        }
        return entries
    }

}