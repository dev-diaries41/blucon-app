package com.fpf.blucon.data.devices.clusters

import com.fpf.blucon.cluster.ClusterCrossRef
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity


class ClusterCrossRefRepository(private val dao: ClusterCrossRefDao) {
    private var clusterToDeviceIdsMap: MutableMap<Long, MutableSet<Long>> = mutableMapOf()
    private var refreshCache: Boolean = false

    suspend fun getAllCrossRefs(): List<ClusterCrossRef> = dao.get().map{it.toDomain()}
    suspend fun getByClusterIds(ids: List<Long>):  List<ClusterCrossRef> = dao.getByClusterIds(ids).map { it.toDomain() }
    suspend fun upsertClusterCrossRefs(crossRefs: List<ClusterCrossRef>) {
        dao.upsert(crossRefs.map { it.toEntity() })
        refreshCache = true
    }

    suspend fun clear() {
        refreshCache = false
        clusterToDeviceIdsMap.clear()
        dao.clear()
    }
    suspend fun count() = dao.count()
    suspend fun count(clusterId: Long) = dao.countByClusterId(clusterId)

    suspend fun getClusterToMediaIdsMap(): Map<Long, MutableSet<Long>> {
        if(refreshCache){
            clusterToDeviceIdsMap.clear()
            refreshCache = false
        }
        if (clusterToDeviceIdsMap.isNotEmpty()) return clusterToDeviceIdsMap

        for(ref in getAllCrossRefs()){
            clusterToDeviceIdsMap.computeIfAbsent(ref.clusterId) { HashSet() }.add(ref.deviceId)
        }
        return clusterToDeviceIdsMap
    }

}