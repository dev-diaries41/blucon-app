package com.fpf.blucon.data.scans

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScanEntryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEntries(entries: List<ScanEntryEntity>): List<Long>

    @Query("""
        SELECT entry.*
        FROM scan_entry entry
        WHERE (scanId =:scanId OR :scanId IS NULL)
            AND (deviceAddress in (:deviceAddresses) OR :deviceAddresses IS NULL)
        ORDER BY timestamp DESC, deviceAddress DESC
    """)
    suspend fun getEntries(scanId: Long?, deviceAddresses: List<String>?): List<ScanEntryEntity>

    @Query("""
        SELECT * from scan_entry 
        WHERE (scanId =:scanId OR :scanId IS NULL)
        ORDER BY timestamp DESC, deviceAddress DESC
        LIMIT :limit OFFSET :offset
        """)
    suspend fun getEntriesDesc(scanId: Long?, limit: Int, offset: Int): List<ScanEntryEntity>


    @Query("""
        SELECT * from scan_entry 
        WHERE (scanId =:scanId OR :scanId IS NULL)
        ORDER BY timestamp ASC, deviceAddress ASC
        LIMIT :limit OFFSET :offset
        """)
    suspend fun getEntriesAsc(scanId: Long?, limit: Int, offset: Int): List<ScanEntryEntity>

    @Query("""
    SELECT *
    FROM scan_entry
    WHERE :query IS NULL
        OR deviceName LIKE '%' || :query || '%'
        OR manufacturerId IN (:manufacturerIds)
    ORDER BY timestamp DESC, deviceAddress DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun queryEntriesDsc(
        query: String,
        manufacturerIds: List<Int>?,
        limit: Int,
        offset: Int
    ): List<ScanEntryEntity>

    @Query("""
    SELECT *
    FROM scan_entry
    WHERE :query IS NULL
        OR deviceName LIKE '%' || :query || '%'
        OR manufacturerId IN (:manufacturerIds)
    ORDER BY timestamp ASC, deviceAddress ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun queryEntriesAsc(
        query: String,
        manufacturerIds: List<Int>,
        limit: Int,
        offset: Int
    ): List<ScanEntryEntity>

    @Delete
    suspend fun deleteEntries(entries: List<ScanEntryEntity>)

    @Query("""
    SELECT COUNT(*)
    FROM scan_entry
    WHERE :query IS NULL
          OR deviceName LIKE '%' || :query || '%'
          OR manufacturerId IN (:manufacturerIds)
""")
    suspend fun countEntries(query: String?, manufacturerIds: List<Int>): Int


    @Query("""
    SELECT manufacturerId, COUNT(*) AS count
    FROM scan_entry
    WHERE (:scanId IS NULL OR scanId = :scanId)
      AND manufacturerId IS NOT NULL
    GROUP BY manufacturerId
    ORDER BY count DESC
    LIMIT COALESCE(:limit, -1)
""")
    suspend fun getManufacturerCounts(scanId: Long?, limit: Int?): List<ManufacturerCount>

    @Query("""
    SELECT deviceName, COUNT(*) AS count
    FROM scan_entry
    WHERE (:scanId IS NULL OR scanId = :scanId)
      AND deviceName IS NOT NULL
    GROUP BY deviceName
    ORDER BY count DESC
    LIMIT COALESCE(:limit, -1)
""")
    suspend fun getDeviceNameCounts(scanId: Long?, limit: Int?): List<DeviceNameCount>
}