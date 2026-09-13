package com.fpf.blucon.data.paging

import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.query.SortBy

class SearchEntriesPagingSource(
    private val scanEntryRepository: ScanEntryRepository,
    private val query: String? = null,
    sortBy: SortBy = SortBy.Date(),
) : DataPagingSource<BTScanEntry, Nothing>(
    filter = null,
    sortBy = sortBy,
) {
    override suspend fun getItems(sortBy: SortBy, pageSize: Int, offset: Int, filter: Nothing?): List<BTScanEntry> {
        val manufacturerIds = scanEntryRepository.queryCompanies(query.orEmpty())

        val entries = if(query.isNullOrBlank()){
            emptyList()
        }else{
            when(sortBy){
                is SortBy.Date -> scanEntryRepository.queryEntries(
                    query=query,
                    limit = pageSize + 1,
                    offset = offset,
                    descending = sortBy.descending,
                    manufacturerIds = manufacturerIds
                )

                is SortBy.NAME -> scanEntryRepository.queryEntriesByName(
                    query=query,
                    limit = pageSize + 1,
                    offset = offset,
                    descending = sortBy.descending,
                    manufacturerIds = manufacturerIds
                )
                is SortBy.RSSI -> error("RSSI Not supported for search")
            }
        }
        return entries
    }

}