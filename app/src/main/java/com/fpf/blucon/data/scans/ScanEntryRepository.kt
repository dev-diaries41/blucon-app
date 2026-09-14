package com.fpf.blucon.data.scans

import com.fpf.blucon.bluetooth.scan.BTScanEntry
import com.fpf.blucon.data.MetadataRepository
import com.fpf.blucon.data.devices.DeviceDao
import com.fpf.blucon.data.devices.DeviceEntity
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity

class ScanEntryRepository(
    private val dao: ScanEntryDao,
    private val deviceDao: DeviceDao,
    private val metadataRepository: MetadataRepository
) {
    suspend fun addEntries(entries: List<BTScanEntry>): List<Long> {
        val ids = dao.addEntries(entries.map { it.toEntity() })
        val names = entries.mapNotNull { it.deviceName }.distinct()
        deviceDao.insert(names.map{ DeviceEntity(name=it) })
        return ids
    }
    suspend fun getEntries(scanId: Long? = null, deviceAddresses: List<String>? = null) =
        dao.getEntries(scanId, deviceAddresses).map { it.toDomain() }

    suspend fun getEntries(limit: Int, offset: Int, scanId: Long? = null, descending: Boolean = true) = if (descending) {
        dao.getEntriesDesc(scanId, limit = limit, offset = offset)
    } else {
        dao.getEntriesAsc(scanId, limit = limit, offset = offset)
    }.map { it.toDomain().copy(manufacturerName = getCompanyName(it.manufacturerId)) }

    suspend fun getEntriesByRssi(
        limit: Int,
        offset: Int,
        scanId: Long? = null,
        descending: Boolean = true
    ) = if (descending) {
        dao.getEntriesByRssiDesc(scanId, limit, offset)
    } else {
        dao.getEntriesByRssiAsc(scanId, limit, offset)
    }.map { it.toDomain().copy(manufacturerName = getCompanyName(it.manufacturerId)) }

    suspend fun getEntriesByName(
        limit: Int,
        offset: Int,
        scanId: Long? = null,
        descending: Boolean = true
    ) = if (descending) {
        dao.getEntriesByNameDesc(scanId, limit, offset)
    } else {
        dao.getEntriesByNameAsc(scanId, limit, offset)
    }.map { it.toDomain().copy(manufacturerName = getCompanyName(it.manufacturerId)) }

    suspend fun queryEntriesByName(
        limit: Int,
        offset: Int,
        query: String,
        manufacturerIds: List<Int> = emptyList(),
        descending: Boolean = true
    ) = if (descending) {
        dao.queryEntriesNameDesc(query, manufacturerIds=manufacturerIds, limit=limit, offset=offset)
    } else {
        dao.queryEntriesNameAsc(query, manufacturerIds=manufacturerIds, limit=limit, offset=offset)
    }.map { it.toDomain().copy(manufacturerName = getCompanyName(it.manufacturerId)) }

    suspend fun queryEntries(
        limit: Int,
        offset: Int,
        query: String,
        manufacturerIds: List<Int> = emptyList(),
        descending: Boolean = true
    ) = if (descending) {
        dao.queryEntriesDsc(
            query,
            limit = limit,
            offset = offset,
            manufacturerIds = manufacturerIds
        )
    } else {
        dao.queryEntriesAsc(
            query,
            limit = limit,
            offset = offset,
            manufacturerIds = manufacturerIds
        )
    }.map { it.toDomain().copy(manufacturerName = getCompanyName(it.manufacturerId)) }

    fun queryCompanies(query: String): List<Int> = metadataRepository.findCompanyIds(query)
    fun getCompanyName(manufacturerId: Int?): String? =
        metadataRepository.getCompanyName(manufacturerId)

    fun getServiceName(serviceId: Int?): String? = metadataRepository.getServiceName(serviceId)

    suspend fun countEntries(query: String?= null, manufacturerIds: List<Int> = emptyList()): Int =
        dao.countEntries(query, manufacturerIds)

    suspend fun getManufacturerCounts(scanId: Long? = null, limit: Int = -1, offset: Int = 0, descending: Boolean = true): List<Pair<String, Int>> = if (descending) {
        dao.getManufacturerCountsDesc(scanId, limit = limit, offset = offset).map { entry ->
           ( getCompanyName(entry.manufacturerId)?: entry.manufacturerId.toString()) to entry.count
        }
    } else {
        dao.getManufacturerCountsAsc(scanId, limit = limit, offset = offset).map { entry ->
            ( getCompanyName(entry.manufacturerId)?: entry.manufacturerId.toString()) to entry.count
        }
    }

    suspend fun getDeviceNameCounts(scanId: Long? = null, limit: Int = -1, offset: Int = 0, descending: Boolean = true): List<Pair<String, Int>> = if (descending) {
        dao.getDeviceNameCountsDesc(scanId, limit = limit, offset = offset).map { entry -> entry.deviceName to entry.count }
    } else {
        dao.getDeviceNameCountsAsc(scanId, limit = limit, offset = offset).map { entry -> entry.deviceName to entry.count }
    }

    suspend fun getUniqueDeviceNames(scanId: Long? = null): List<String> = dao.getUniqueDevices(scanId)
}

