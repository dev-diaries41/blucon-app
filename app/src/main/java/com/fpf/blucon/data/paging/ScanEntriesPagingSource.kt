package com.fpf.blucon.data.paging

import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.data.MetadataRepository
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.query.SortBy

class ScanEntriesPagingSource(
    private val scanId: Long,
    private val scanEntryRepository: ScanEntryRepository,
    private val metadataRepository: MetadataRepository,
    private val query: String? = null,
    sortBy: SortBy = SortBy.Date(),
) : DataPagingSource<BTScanEntry, Nothing>(
    filter = null,
    sortBy = sortBy,
) {
    override suspend fun getItems(sortBy: SortBy, pageSize: Int, offset: Int, filter: Nothing?): List<BTScanEntry> {
        val entries = if(!query.isNullOrBlank()){
            scanEntryRepository.queryEntries(
                query=query,
                scanId=scanId,
                limit = pageSize + 1,
                offset = offset,
                descending = sortBy.descending
            )
        }else{
            scanEntryRepository.getEntries(
                scanId=scanId,
                limit = pageSize + 1,
                offset = offset,
                descending = sortBy.descending
            )
        }

        return entries.map{it.copy(manufacturerName = metadataRepository.getCompanyName(it.manufacturerId))}
    }

}