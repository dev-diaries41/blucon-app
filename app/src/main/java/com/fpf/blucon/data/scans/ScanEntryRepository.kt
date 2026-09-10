package com.fpf.blucon.data.scans

import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.data.mappers.toEntity

class ScanEntryRepository(private val dao: ScanEntryDao) {
    suspend fun addEntries(entries: List<BTScanEntry>): List<Long> = dao.addEntries(entries.map{it.toEntity()})
    suspend fun getEntries(scanId: Long? = null, deviceAddresses: List<String>? = null) = dao.getEntries(scanId, deviceAddresses)
    suspend fun getEntries(scanId: Long? = null, deviceAddress: String? = null, limit: Int, offset: Int) = dao.getEntries(scanId, limit=limit, offset=offset)
}