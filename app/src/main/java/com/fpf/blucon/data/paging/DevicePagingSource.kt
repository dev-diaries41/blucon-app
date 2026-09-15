package com.fpf.blucon.data.paging


import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.data.devices.DeviceRepository
import com.fpf.blucon.query.SortBy

class DevicePagingSource(
    private val deviceRepository: DeviceRepository,
    sortBy: SortBy = SortBy.Name(),
) : DataPagingSource<DeviceInfo, Nothing>(
    filter = null,
    sortBy = sortBy,
) {
    override suspend fun getItems(sortBy: SortBy, pageSize: Int, offset: Int, filter: Nothing?): List<DeviceInfo> {
        val entries = deviceRepository.getPage(
            limit = pageSize + 1,
            offset = offset,
            descending = sortBy.descending
        )
        return entries
    }
}