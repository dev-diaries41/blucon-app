package com.fpf.blucon.data.scans

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fpf.blucon.data.CountData

@Dao
interface ScanEntryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEntries(entries: List<ScanEntryEntity>): List<Long>

    @Query("""
        SELECT DISTINCT deviceName
        FROM scan_entry entry
        WHERE (scanId =:scanId OR :scanId IS NULL)
            AND deviceName iS NOT NULL
        ORDER BY timestamp DESC, deviceName DESC
    """)
    suspend fun getUniqueDevices(scanId: Long?): List<String>

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
    SELECT * FROM scan_entry
    WHERE (scanId = :scanId OR :scanId IS NULL)
    ORDER BY rssi DESC, deviceAddress DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getEntriesByRssiDesc(scanId: Long?, limit: Int, offset: Int): List<ScanEntryEntity>

    @Query("""
    SELECT * FROM scan_entry
    WHERE (scanId = :scanId OR :scanId IS NULL)
    ORDER BY rssi ASC, deviceAddress ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getEntriesByRssiAsc(scanId: Long?, limit: Int, offset: Int): List<ScanEntryEntity>

    @Query("""
    SELECT * FROM scan_entry
    WHERE (scanId = :scanId OR :scanId IS NULL)
    ORDER BY deviceName DESC, deviceAddress DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getEntriesByNameDesc(scanId: Long?, limit: Int, offset: Int): List<ScanEntryEntity>

    @Query("""
    SELECT * FROM scan_entry
    WHERE (scanId = :scanId OR :scanId IS NULL)
    ORDER BY deviceName ASC, deviceAddress ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getEntriesByNameAsc(scanId: Long?, limit: Int, offset: Int): List<ScanEntryEntity>


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

    @Query("""
    SELECT *
    FROM scan_entry
    WHERE :query IS NULL
        OR deviceName LIKE '%' || :query || '%'
        OR manufacturerId IN (:manufacturerIds)
    ORDER BY deviceName ASC, deviceAddress ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun queryEntriesNameAsc(
        query: String?,
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
    ORDER BY deviceName DESC, deviceAddress DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun queryEntriesNameDesc(
        query: String?,
        manufacturerIds: List<Int>?,
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
    LIMIT :limit OFFSET :offset
""")
    suspend fun getManufacturerCountsDesc(scanId: Long?, limit: Int, offset: Int): List<ManufacturerCount>

    @Query("""
    SELECT manufacturerId, COUNT(*) AS count
    FROM scan_entry
    WHERE (:scanId IS NULL OR scanId = :scanId)
      AND manufacturerId IS NOT NULL
    GROUP BY manufacturerId
    ORDER BY count ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getManufacturerCountsAsc(scanId: Long?, limit: Int, offset: Int): List<ManufacturerCount>


    @Query("""
    SELECT deviceName, COUNT(*) AS count
    FROM scan_entry
    WHERE (:scanId IS NULL OR scanId = :scanId)
      AND deviceName IS NOT NULL
    GROUP BY deviceName
    ORDER BY count DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getDeviceNameCountsDesc(scanId: Long?, limit: Int, offset: Int): List<DeviceNameCount>

    @Query("""
    SELECT deviceName, COUNT(*) AS count
    FROM scan_entry
    WHERE (:scanId IS NULL OR scanId = :scanId)
      AND deviceName IS NOT NULL
    GROUP BY deviceName
    ORDER BY count ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getDeviceNameCountsAsc(scanId: Long?, limit: Int, offset: Int): List<DeviceNameCount>

    @Query("""
    SELECT c.clusterId AS id, c.label AS name, COUNT(*) AS count
    FROM scan_entry se
    INNER JOIN device_name d ON d.name = se.deviceName
    INNER JOIN device_cluster_crossref cc ON cc.deviceId = d.id
    INNER JOIN device_cluster c ON c.clusterId = cc.clusterId
    WHERE (:scanId IS NULL OR se.scanId = :scanId)
    GROUP BY c.clusterId, c.label
    ORDER BY count ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getClusterCountsAsc(
        scanId: Long?,
        limit: Int,
        offset: Int
    ): List<CountData>

    @Query("""
    SELECT c.clusterId AS id, c.label AS name, COUNT(*) AS count
    FROM scan_entry se
    INNER JOIN device_name d ON d.name = se.deviceName
    INNER JOIN device_cluster_crossref cc ON cc.deviceId = d.id
    INNER JOIN device_cluster c ON c.clusterId = cc.clusterId
    WHERE (:scanId IS NULL OR se.scanId = :scanId)
    GROUP BY c.clusterId, c.label
    ORDER BY count DESC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getClusterCountsDesc(
        scanId: Long?,
        limit: Int,
        offset: Int
    ): List<CountData>

}