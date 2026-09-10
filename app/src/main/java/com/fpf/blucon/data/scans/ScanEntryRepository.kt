package com.fpf.blucon.data.scans

import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity

class ScanEntryRepository(private val dao: ScanEntryDao) {
    suspend fun addEntries(entries: List<BTScanEntry>): List<Long> = dao.addEntries(entries.map{it.toEntity()})
    suspend fun getEntries(scanId: Long? = null, deviceAddresses: List<String>? = null) = dao.getEntries(scanId, deviceAddresses).map{it.toDomain()}
    suspend fun getEntries(limit: Int, offset: Int, scanId: Long? = null, deviceAddress: String? = null, descending: Boolean = true) = if(descending){
            dao.getEntriesDesc(scanId, limit=limit, offset=offset)
        }else{
            dao.getEntriesAsc(scanId, limit=limit, offset=offset)
        }.map{it.toDomain()}
}