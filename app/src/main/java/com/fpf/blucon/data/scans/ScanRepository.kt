package com.fpf.blucon.data.scans

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.NewBTScan
import com.fpf.blucon.data.MetadataRepository
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity

class ScanRepository(private val dao: ScanDao, private val metadataRepository: MetadataRepository) {
    suspend fun insertScan(scan: NewBTScan): Long = dao.insertScan(scan.toEntity())
    suspend fun getScans(limit: Int, offset: Int, startDate: Long? = null, endDate: Long? = null, descending: Boolean = true): List<BTScan> =
        if(descending){
            dao.getScansDesc(limit =limit, offset=offset, startDate=startDate, endDate=endDate).map{it.toDomain()}
        }else{
            dao.getScansAsc(limit =limit, offset=offset, startDate=startDate, endDate=endDate).map{it.toDomain()}
        }
    suspend fun getScans(scanIds: List<Long>? = null, startDate: Long? = null, endDate: Long? = null): List<BTScan> = dao.getScans(scanIds, startDate, endDate).map{it.toDomain()}
    suspend fun deleteScans(scans: List<BTScan>) = dao.deleteScans(scans.map{it.toEntity()})

    suspend fun deleteScansById(ids: List<Long>) = dao.deleteScansById(ids)
    suspend fun countScans(): Int = dao.countScans()
    suspend fun clearScans() = dao.clearScans()

    fun getPostcode(long: Double, lat: Double): String? = metadataRepository.getPostcode(longitude = long, latitude = lat)
}