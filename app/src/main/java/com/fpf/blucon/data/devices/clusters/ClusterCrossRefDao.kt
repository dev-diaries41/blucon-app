package com.fpf.blucon.data.devices.clusters

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClusterCrossRefDao {

    @Query("SELECT * FROM device_cluster_crossref")
    suspend fun get(): List<ClusterCrossRefEntity>

    @Query("SELECT * FROM device_cluster_crossref WHERE clusterId in (:ids)")
    suspend fun getByClusterIds(ids: List<Long>): List<ClusterCrossRefEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(crossRefs: List<ClusterCrossRefEntity>)

    @Query("DELETE FROM device_cluster_crossref")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM device_cluster_crossref")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM device_cluster_crossref WHERE clusterId = :clusterId")
    suspend fun countByClusterId(clusterId: Long): Int
}