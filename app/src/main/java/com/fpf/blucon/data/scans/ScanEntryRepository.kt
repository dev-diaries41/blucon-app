package com.fpf.blucon.data.scans

import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.data.MetadataRepository
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity

class ScanEntryRepository(
    private val dao: ScanEntryDao,
    private val metadataRepository: MetadataRepository
) {
    suspend fun addEntries(entries: List<BTScanEntry>): List<Long> = dao.addEntries(entries.map{it.toEntity()})
    suspend fun getEntries(scanId: Long? = null, deviceAddresses: List<String>? = null) = dao.getEntries(scanId, deviceAddresses).map{it.toDomain()}
    suspend fun getEntries(limit: Int, offset: Int, scanId: Long? = null, descending: Boolean = true) = if(descending){
            dao.getEntriesDesc(scanId, limit=limit, offset=offset)
        }else{
            dao.getEntriesAsc(scanId, limit=limit, offset=offset)
        }.map{it.toDomain().copy(manufacturerName = metadataRepository.getCompanyName(it.manufacturerId))}

    suspend fun queryEntries(limit: Int, offset: Int, query: String, manufacturerIds: List<Int> = emptyList(), descending: Boolean = true) = if(descending){
        dao.queryEntriesDsc(query, limit=limit, offset=offset, manufacturerIds=manufacturerIds)
    }else{
        dao.queryEntriesAsc(query, limit=limit, offset=offset, manufacturerIds=manufacturerIds)
    }.map{it.toDomain(metadataRepository.getCompanyName(it.manufacturerId))}

    suspend fun countEntries(query: String, manufacturerIds: List<Int> = emptyList()): Int = dao.countEntries(query, manufacturerIds)
}

