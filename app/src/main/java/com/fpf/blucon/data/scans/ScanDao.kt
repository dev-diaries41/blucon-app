package com.fpf.blucon.data.scans

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScan(scan: ScanEntity): Long

    @Query("""
        SELECT s.*, COUNT(entry.deviceAddress) AS count
        FROM scan s
        LEFT JOIN scan_entry entry ON s.id = entry.scanId
        WHERE (:startDate IS NULL OR s.timestamp >= :startDate)
            AND (:endDate IS NULL OR s.timestamp <= :endDate)
            AND (:scanIds IS NULL OR s.id IN (:scanIds) )
        GROUP by s.id
        ORDER BY s.timestamp
        """)
    suspend fun getScans(scanIds: List<Long>?, startDate: Long?, endDate: Long?): List<ScanWithCount>

    @Query("""
        SELECT s.*, COUNT(entry.deviceAddress) AS count
        FROM scan s
        LEFT JOIN scan_entry entry ON s.id = entry.scanId
         WHERE (:startDate IS NULL OR s.timestamp >= :startDate)
            AND (:endDate IS NULL OR s.timestamp <= :endDate)
        GROUP by s.id
        ORDER BY s.timestamp
        LIMIT :limit OFFSET :offset
        """)
    suspend fun getScans(limit: Int, offset: Int, startDate: Long?, endDate: Long?): List<ScanWithCount>

    @Delete
    suspend fun deleteScans(scans: List<ScanEntity>)

    @Query("SELECT COUNT(*) FROM scan")
    suspend fun countScans(): Int
}