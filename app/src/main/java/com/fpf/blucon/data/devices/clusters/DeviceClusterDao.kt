package com.fpf.blucon.data.devices.clusters

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceClusterDao {

    @Query(
        """
        SELECT meta.*
        FROM device_cluster AS meta
        INNER JOIN device_cluster_crossref AS crossref
            ON meta.clusterId = crossref.clusterId
        WHERE crossref.deviceId = :deviceId
        """
    )
    suspend fun getClustersForDevice(deviceId: Long): List<DeviceClusterEntity>
    @Query(
        """
    SELECT DISTINCT meta.*
    FROM device_cluster AS meta
    INNER JOIN device_cluster_crossref AS crossref
        ON meta.clusterId = crossref.clusterId
    WHERE crossref.deviceId IN (:mediaIds)
    """
    )
    suspend fun getClustersForDevices(mediaIds: List<Long>): List<DeviceClusterEntity>


    @Query("""
    SELECT
        clusterId,
        label,
        prototypeSize AS size
    FROM device_cluster
    WHERE (:clusterIds IS NULL OR clusterId IN (:clusterIds))
    ORDER BY prototypeSize DESC
""")
    suspend fun getCollections(clusterIds: List<Long>?): List<DeviceCollectionData>

    @Query("""
    SELECT
        clusterId,
        label,
        prototypeSize AS size
    FROM device_cluster
    WHERE (:clusterIds IS NULL OR clusterId IN (:clusterIds))
    ORDER BY prototypeSize DESC
""")
    fun getCollectionsFlow(clusterIds: List<Long>?): Flow<List<DeviceCollectionData>>
    @Query("""
    SELECT metadata.*, COUNT(crossRef.deviceId) AS prototypeSize
    FROM device_cluster metadata
    JOIN device_cluster_crossref crossRef ON metadata.clusterId = crossRef.clusterId
    GROUP BY metadata.clusterId
""")
    suspend fun get(): List<DeviceClusterEntity>

    @Query("""
    SELECT metadata.*, COUNT(crossRef.deviceId) AS prototypeSize
    FROM device_cluster metadata
    JOIN device_cluster_crossref crossRef ON metadata.clusterId = crossRef.clusterId
    WHERE metadata.clusterId IN (:ids)
    GROUP BY metadata.clusterId
""")
    suspend fun get(ids: List<Long>): List<DeviceClusterEntity>

    @Query("SELECT clusterId FROM device_cluster metadata")
    suspend fun getIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(clusters: List<DeviceClusterEntity>): List<Long>

    @Update
    suspend fun update(clusters: List<DeviceClusterEntity>)

    @Transaction
    @Query("DELETE FROM device_cluster WHERE clusterId IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM device_cluster WHERE prototypeSize >= :minSize")
    suspend fun count(minSize: Int = 1): Int

    @Query("DELETE FROM device_cluster")
    suspend fun clear()

}

