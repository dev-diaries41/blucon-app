package com.fpf.blucon.data.devices.clusters

import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.cluster.StoredClusterMetadata
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.associate

class DeviceClusterRepository(private val dao: DeviceClusterDao) {
    suspend fun getAllMetadataAsMap(): Map<Long, StoredClusterMetadata> = dao.get().associate {
            it.clusterId to it.toDomain()
        }

    fun getCollectionsFlow(clusterIds: List<Long>? = null): Flow<List<DeviceCollection>> = dao.getCollectionsFlow(clusterIds).map{ collections -> collections.map{it.toDomain()}}

    suspend fun getClusterIds(): List<Long> = dao.getIds()
    suspend fun getClustersForDevice(deviceId: Long): List<StoredClusterMetadata> = dao.getClustersForDevice(deviceId).map{it.toDomain()}
    suspend fun getClustersForDevices(deviceIds: List<Long>): List<StoredClusterMetadata> = dao.getClustersForDevices(deviceIds).map{it.toDomain()}
    suspend fun getCollectionsOrderedByName(clusterIds: List<Long>? = null, limit: Int? = null, offset: Int = 0, descending: Boolean = true): List<DeviceCollection> = if(descending) {
        dao.getCollectionsByNameDesc(clusterIds, limit, offset).map { it.toDomain() }
    }else{
        dao.getCollectionsByNameAsc(clusterIds, limit, offset).map { it.toDomain() }
    }
    suspend fun getCollections(clusterIds: List<Long>? = null, limit: Int? = null, offset: Int = 0, descending: Boolean = true): List<DeviceCollection> = if(descending) {
        dao.getCollectionsBySizeDesc(clusterIds, limit, offset).map { it.toDomain() }
    }else{
        dao.getCollectionsBySizeAsc(clusterIds, limit, offset).map { it.toDomain() }
    }

    suspend fun getCollectionsByName(names: List<String>? = null, limit: Int? = null): List<DeviceCollection> = dao.getCollectionsByName(names, limit).map{ it.toDomain()}

    suspend fun getClusters(ids: List<Long>): List<StoredClusterMetadata> = dao.get(ids).map{it.toDomain()}
    suspend fun getCluster(id: Long): StoredClusterMetadata? = dao.get(listOf(id)).firstOrNull()?.toDomain()
    suspend fun getClusterByName(name: String): StoredClusterMetadata? = dao.getByName(name)?.toDomain()

    suspend fun count(minSize: Int = 1): Int = dao.count(minSize)
    suspend fun insertMetadata(metadataBatch: List<StoredClusterMetadata>) = dao.insert(metadataBatch.map{it.toEntity()})
    suspend fun insertMetadata(metadata: StoredClusterMetadata) = dao.insert(listOf(metadata.toEntity()))

    suspend fun updateMetadata(metadataBatch: List<StoredClusterMetadata>) = dao.update(metadataBatch.map{it.toEntity()})
    suspend fun updateMetadata(metadata: StoredClusterMetadata) = dao.update(listOf(metadata.toEntity()))

    suspend fun deleteMetadata(ids: List<Long>) = dao.delete(ids)
    suspend fun deleteMetadata(id: Long) = dao.delete(listOf(id))

    suspend fun clear() = dao.clear()
}