package com.fpf.blucon.index

import android.app.Application
import com.fpf.blucon.cluster.ClusterManager
import com.fpf.blucon.data.devices.DeviceRepository
import com.fpf.smartscansdk.core.embeddings.FileEmbeddingStore
import com.fpf.smartscansdk.core.embeddings.TextEmbeddingProvider
import com.fpf.smartscansdk.core.processors.Concurrency
import com.fpf.smartscansdk.core.processors.ConcurrencyController
import com.fpf.smartscansdk.core.processors.ProcessorResult

class IndexJobManager(
    private val application: Application,
    private val textEmbedder: TextEmbeddingProvider,
    private val deviceEmbedStore: FileEmbeddingStore,
    private val deviceRepository: DeviceRepository,
    private val clusterManager: ClusterManager,
    private val useListener: Boolean = true
) {
    companion object {
        private const val TAG = "IndexJobManager"
    }

    suspend fun run(): ProcessorResult{
            if(!textEmbedder.isInitialized()) textEmbedder.initialize()

            val concurrencyController = ConcurrencyController(application)
            val concurrency = Concurrency.Dynamic{ concurrencyController.calculateConcurrency() }

            val indexer = DeviceIndexer(
                embedder = textEmbedder,
                listener = if (useListener) DeviceIndexListener else null,
                deviceEmbedStore = deviceEmbedStore,
                deviceRepository = deviceRepository,
                quantize = true,
                concurrency = concurrency
            )

            val indexResult = indexer.index()

            try {
                val unclusteredItemIdsMap = deviceRepository.getUnclusteredItems()
                clusterManager.cluster(unclusteredItemIdsMap)
            } catch (e: Exception) {
                error("cluster error")
//                throw AppException.ClusterException(cause = e)
            }
            return indexResult
        }
    }