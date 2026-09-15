package com.fpf.blucon.data.devices

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(deviceNames: List<DeviceEntity>): List<Long>

    @Query("DELETE FROM device_name WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT * FROM device_name WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<DeviceEntity>

    @Query("SELECT * FROM device_name WHERE name IN (:names)")
    suspend fun getByNames(names: List<String>): List<DeviceEntity>

    @Query("SELECT * FROM device_name")
    suspend fun get(): List<DeviceEntity>

    @Query("SELECT * FROM device_name ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getPageAsc(limit: Int, offset: Int): List<DeviceEntity>

    @Query("SELECT * FROM device_name ORDER BY name DESC LIMIT :limit OFFSET :offset")
    suspend fun getPageDesc(limit: Int, offset: Int): List<DeviceEntity>

    @Query("""
        SELECT id 
        FROM device_name
        WHERE NOT EXISTS (
                SELECT 1
                FROM device_cluster_crossref c
                WHERE c.deviceId = device_name.id
        )
        """
    )
    suspend fun getUnclusteredItemIds(): List<Long>

    @Query("""
    SELECT d.*
    FROM device_name d
    INNER JOIN device_cluster_crossref c
        ON c.deviceId = d.id
    WHERE c.clusterId = :clusterId
    ORDER BY d.name DESC, d.id DESC
""")
    suspend fun getByCluster(clusterId: Long): List<DeviceEntity>
}