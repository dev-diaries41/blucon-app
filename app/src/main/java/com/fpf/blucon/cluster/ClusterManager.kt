package com.fpf.blucon.cluster

import android.content.Context
import android.util.Log
import com.fpf.blucon.data.devices.clusters.ClusterCrossRefRepository
import com.fpf.blucon.data.mappers.toIncrementalClusterMetadata
import com.fpf.blucon.embeds.EmbeddingStoresFiles
import com.fpf.blucon.data.devices.clusters.DeviceClusterRepository
import com.fpf.smartscansdk.core.cluster.Cluster
import com.fpf.smartscansdk.core.cluster.ClusterResult
import com.fpf.smartscansdk.core.cluster.IncrementalClusterer
import com.fpf.smartscansdk.core.embeddings.Embedding
import com.fpf.smartscansdk.core.embeddings.FileEmbeddingStore
import com.fpf.smartscansdk.core.embeddings.StoredEmbedding
import com.fpf.smartscansdk.core.embeddings.generatePrototypeEmbedding
import com.fpf.smartscansdk.core.embeddings.getSimilarities
import com.fpf.smartscansdk.core.embeddings.toQInt8Embed
import java.io.File
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.math.sqrt

class ClusterManager(
    private val clusterEmbedStore: FileEmbeddingStore,
    private val deviceEmbedStore: FileEmbeddingStore,
    private val clusterCrossRefRepository: ClusterCrossRefRepository,
    private val clusterRepository: DeviceClusterRepository,
) {
    companion object {
        private const val THRESHOLD: Float = 0.7f

        const val TAG = "ClusterManager"
    }

    private var idCount: Long = 0L

    val allCollectionsFlow = clusterRepository.getCollectionsFlow()

    suspend fun cluster(unclusterItems: List<Long> ) {
        val unclusterItemEmbeds = deviceEmbedStore.get(unclusterItems)
        if(unclusterItemEmbeds.isEmpty()) return

        val existingClusters: Map<Long, Cluster> = getAllClusters()
        val clusterer = IncrementalClusterer(existingClusters = existingClusters, defaultThreshold = THRESHOLD, similarityAlpha = 0.975f)
        val result = clusterer.cluster(unclusterItemEmbeds.associate { it.id to it.embedding})
        updateClustersAndAssign(result, existingClusters.keys)
    }

    suspend fun sync(clusterId: Long){
        val clusterCrossRefs = clusterCrossRefRepository.getByClusterIds(listOf(clusterId))

        // Delete cluster from embed store if there is no items in cluster
        if(clusterCrossRefs.isEmpty()) {
            clusterEmbedStore.remove(listOf(clusterId))
            return
        }

        val embeddings = deviceEmbedStore.get(clusterCrossRefs.map{it.deviceId}).map{it.embedding}
        val (prototypeEmbedding, meanSim, stdSim) = computeClusterMetrics(embeddings)
        val oldStoredEmbed = clusterEmbedStore.get(clusterId)?: error("Cluster embedding not found")
        val updatedStoredEmbed = oldStoredEmbed.copy(embedding = prototypeEmbedding)
        val clusterMetadata = clusterRepository.getCluster(clusterId)?: return
        val updatedMetadata = clusterMetadata.copy(meanSimilarity = meanSim, stdSimilarity = stdSim, prototypeSize = embeddings.size)
        clusterEmbedStore.update(updatedStoredEmbed)
        clusterRepository.updateMetadata(updatedMetadata)
    }

    suspend fun syncEmbedsWithRoom(){
        val clusterIdsFromRoom = clusterRepository.getClusterIds().toSet()
        val clusterIdsFromEmbedStore = clusterEmbedStore.get().map{it.id}
        val clustersIdsToPurge = clusterIdsFromEmbedStore.filterNot { it in clusterIdsFromRoom}
        if(clustersIdsToPurge.isNotEmpty()) {
            val removed = clusterEmbedStore.remove(clustersIdsToPurge)
            Log.d(TAG, "Purged $removed clusters")
        }
    }

    suspend fun mergeClusters(primaryClusterId: Long, otherClusters: List<Long>){
        val otherClustersCrossRefs = clusterCrossRefRepository.getByClusterIds(otherClusters)
        val updatedClusterCrossRefs = otherClustersCrossRefs.map { it.copy(clusterId = primaryClusterId) }
        clusterCrossRefRepository.upsertClusterCrossRefs(updatedClusterCrossRefs)

        // Delete clusters which are being merged (cascades all related crossrefs)
        clusterRepository.deleteMetadata(otherClusters)
        clusterEmbedStore.remove(otherClusters)
        sync(primaryClusterId)
    }

    suspend fun moveItems(items: List<Long>, newClusterId: Long, oldClusterId: Long){
        val crossRefs = items.map {
            ClusterCrossRef(clusterId = newClusterId, deviceId = it)
        }
        clusterCrossRefRepository.upsertClusterCrossRefs(crossRefs)
        listOf(oldClusterId, newClusterId).forEach { sync(it) }
    }

    suspend fun createNewClusterAndMoveItems(items: List<Long>, newClusterLabel: String, oldClusterId: Long){
        val itemEmbeds = deviceEmbedStore.get(items)
        createNewCluster(itemEmbeds, newClusterLabel)
        sync(oldClusterId)
    }

    suspend fun updateLabel(clusterId: Long, newLabel: String){
        val metadata = clusterRepository.getCluster(clusterId)?: return
        val updatedMeta = metadata.copy(label = newLabel)
        clusterRepository.updateMetadata(updatedMeta)
    }

    suspend fun getClustersMatchingMedia(deviceId: Long): List<StoredClusterMetadata>{
        return clusterRepository.getClustersForDevice(deviceId)
    }

    suspend fun getClustersMatchingMedia(deviceIds: List<Long>): List<StoredClusterMetadata>{
        return clusterRepository.getClustersForDevices(deviceIds)
    }
    suspend fun deleteAllClusters(context: Context){
        File(context.filesDir, EmbeddingStoresFiles.DEVICE_CLUSTER).delete()
        clusterRepository.clear() // cascades crossrefs
    }

    private suspend fun getAllClusters(): Map<Long, Cluster> {
        return if (clusterEmbedStore.exists) {
            val metadataMap = clusterRepository.getAllMetadataAsMap()

            clusterEmbedStore.get().mapNotNull { cluster ->
                metadataMap[cluster.id]?.let { meta ->
                    cluster.id to Cluster(cluster.id, cluster.embedding, meta.toIncrementalClusterMetadata())
                }
            }.toMap()
        } else emptyMap()
    }

    private suspend fun updateClustersAndAssign(clusterResult: ClusterResult, existingClusterIds: Set<Long>) {
        val (existingClusters, newClusters) = clusterResult.clusters.values.partition { it.clusterId in existingClusterIds }

        val existingMetadata = existingClusters.map {
            StoredClusterMetadata(
                clusterId = it.clusterId,
                prototypeSize = it.metadata.prototypeSize,
                meanSimilarity = it.metadata.meanSimilarity,
                stdSimilarity = it.metadata.stdSimilarity,
                label = it.metadata.label
            )
        }

        val newMetadata = newClusters.map {
            StoredClusterMetadata(
                clusterId = it.clusterId,
                prototypeSize = it.metadata.prototypeSize,
                meanSimilarity = it.metadata.meanSimilarity,
                stdSimilarity = it.metadata.stdSimilarity,
                label = it.metadata.label
            )
        }

        clusterRepository.updateMetadata(existingMetadata)
        clusterRepository.insertMetadata(newMetadata)

        val existingEmbeds = existingClusters.map {
            StoredEmbedding(
                id = it.clusterId,
                embedding = it.embedding.toQInt8Embed(),
                date = System.currentTimeMillis()
            )
        }

        val newEmbeds = newClusters.map {
            StoredEmbedding(
                id = it.clusterId,
                embedding = it.embedding.toQInt8Embed(),
                date = System.currentTimeMillis()
            )
        }

        clusterEmbedStore.add(newEmbeds)
        clusterEmbedStore.update(existingEmbeds)

        val crossRefs = clusterResult.assignments.mapNotNull {
            ClusterCrossRef(clusterId = it.value, deviceId = it.key)
        }
        clusterCrossRefRepository.upsertClusterCrossRefs(crossRefs)
    }

    private suspend fun createNewCluster(itemEmbeds: List<StoredEmbedding>, clusterLabel: String): Long{
        val clusterId = generateId()
        val (metadata, prototype ) = if(itemEmbeds.size == 1) {
            val defaultThreshold = getDefaultThreshold(getAllClusters())
            val meta = StoredClusterMetadata(
                clusterId = clusterId,
                prototypeSize = itemEmbeds.size,
                meanSimilarity = defaultThreshold,
                stdSimilarity = 0f,
                label = clusterLabel,
            )
            val prototypeEmbedding = itemEmbeds.first().embedding
            Pair(meta, prototypeEmbedding)
        }else{
            val (prototypeEmbedding, meanSim, stdSim) = computeClusterMetrics(itemEmbeds.map { it.embedding })

            val meta = StoredClusterMetadata(
                clusterId = clusterId,
                prototypeSize = itemEmbeds.size,
                meanSimilarity = meanSim,
                stdSimilarity = stdSim,
                label = clusterLabel,
            )
            Pair(meta, prototypeEmbedding)
        }

        val clusterEmbed =  StoredEmbedding(
            id = clusterId,
            embedding = prototype.toQInt8Embed(),
            date = System.currentTimeMillis()
        )
        clusterEmbedStore.add(listOf(clusterEmbed))
        clusterRepository.insertMetadata(metadata)

        val crossRefs = itemEmbeds.map {
            ClusterCrossRef(deviceId = it.id, clusterId = clusterEmbed.id)
        }
        clusterCrossRefRepository.upsertClusterCrossRefs(crossRefs)
        return clusterEmbed.id
    }

    private fun getDefaultThreshold(clusters: Map<Long, Cluster>): Float = clusters.values.map{it.metadata.meanSimilarity - it.metadata.stdSimilarity}.average().toFloat()

    private fun computeClusterMetrics(embeddings: List<Embedding> ): Triple<Embedding, Float, Float>{
        val prototypeEmbedding = generatePrototypeEmbedding(embeddings)
        val sims = getSimilarities(prototypeEmbedding, embeddings)
        val meanSim = sims.average().toFloat()
        val stdSim = sqrt(sims.map { (it - meanSim) * (it - meanSim) }.average()).toFloat()
        return Triple(prototypeEmbedding, meanSim, stdSim)
    }

    private fun generateId(): Long {
        val id = System.currentTimeMillis() + idCount
        ++idCount
        return id
    }
}