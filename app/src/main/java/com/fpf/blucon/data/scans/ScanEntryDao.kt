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
    SELECT
        e.deviceAddress,
        e.deviceName,
        e.manufacturerId,
        e.rssi,
        e.timestamp AS lastSeen,
        (
            SELECT COUNT(*)
            FROM scan_entry x
            WHERE x.deviceAddress = e.deviceAddress
        ) AS scanCount
    FROM scan_entry e
    WHERE e.timestamp = (
        SELECT MAX(x.timestamp)
        FROM scan_entry x
        WHERE x.deviceAddress = e.deviceAddress
    )
      AND (:query IS NULL OR e.deviceName LIKE '%' || :query || '%')
      AND (:manufacturerIds IS NULL OR e.manufacturerId IN (:manufacturerIds))
    ORDER BY lastSeen DESC, e.deviceAddress DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun queryEntriesDsc(
        query: String?,
        manufacturerIds: List<Int>?,
        limit: Int,
        offset: Int
    ): List<DeviceSummaryEntity>

    @Query("""
    SELECT
        e.deviceAddress,
        e.deviceName,
        e.manufacturerId,
        e.rssi,
        e.timestamp AS lastSeen,
        (
            SELECT COUNT(*)
            FROM scan_entry x
            WHERE x.deviceAddress = e.deviceAddress
        ) AS scanCount
    FROM scan_entry e
    WHERE e.timestamp = (
        SELECT MAX(x.timestamp)
        FROM scan_entry x
        WHERE x.deviceAddress = e.deviceAddress
    )
      AND (:query IS NULL OR e.deviceName LIKE '%' || :query || '%')
      AND (:manufacturerIds IS NULL OR e.manufacturerId IN (:manufacturerIds))
    ORDER BY lastSeen ASC, e.deviceAddress ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun queryEntriesAsc(
        query: String?,
        manufacturerIds: List<Int>?,
        limit: Int,
        offset: Int
    ): List<DeviceSummaryEntity>

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

}