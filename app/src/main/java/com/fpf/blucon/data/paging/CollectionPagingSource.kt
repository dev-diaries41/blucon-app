package com.fpf.blucon.data.paging

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.data.devices.clusters.DeviceClusterRepository
import com.fpf.blucon.query.SortBy

class CollectionPagingSource(
    private val deviceClusterRepository: DeviceClusterRepository,
    sortBy: SortBy = SortBy.Size(),
) : DataPagingSource<DeviceCollection, Nothing>(
    filter = null,
    sortBy = sortBy,
) {
    override suspend fun getItems(sortBy: SortBy, pageSize: Int, offset: Int, filter: Nothing?): List<DeviceCollection> {
        val entries = when(sortBy){
            is SortBy.Name -> deviceClusterRepository.getCollectionsOrderedByName(
                limit = pageSize + 1,
                offset = offset,
                descending = sortBy.descending
            )
            else -> deviceClusterRepository.getCollections(
                limit = pageSize + 1,
                offset = offset,
                descending = sortBy.descending
            )
        }
        return entries
    }
}