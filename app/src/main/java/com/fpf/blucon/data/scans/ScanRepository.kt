package com.fpf.blucon.data.scans

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.NewBTScan
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity

class ScanRepository(private val dao: ScanDao) {
    suspend fun insertScan(scan: NewBTScan): Long = dao.insertScan(scan.toEntity())
    suspend fun getScans(limit: Int, offset: Int, startDate: Long? = null, endDate: Long? = null, descending: Boolean = true): List<BTScan> =
        if(descending){
            dao.getScansDesc(limit =limit, offset=offset, startDate=startDate, endDate=endDate).map{it.toDomain()}
        }else{
            dao.getScansAsc(limit =limit, offset=offset, startDate=startDate, endDate=endDate).map{it.toDomain()}
        }
    suspend fun getScans(scanIds: List<Long>? = null, startDate: Long? = null, endDate: Long? = null): List<BTScan> = dao.getScans(scanIds, startDate, endDate).map{it.toDomain()}
    suspend fun deleteScans(scans: List<BTScan>) = dao.deleteScans(scans.map{it.toEntity()})

    suspend fun deleteScans(ids: List<Long>) = dao.deleteScans(ids)
    suspend fun countScans(): Int = dao.countScans()
    suspend fun clearScans() = dao.clearScans()
}