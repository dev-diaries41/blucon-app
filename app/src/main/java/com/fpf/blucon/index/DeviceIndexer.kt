package com.fpf.blucon.index

import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.data.devices.DeviceRepository
import com.fpf.smartscansdk.core.embeddings.Embedding
import com.fpf.smartscansdk.core.embeddings.EmbeddingStore
import com.fpf.smartscansdk.core.embeddings.StoredEmbedding
import com.fpf.smartscansdk.core.embeddings.TextEmbeddingProvider
import com.fpf.smartscansdk.core.embeddings.toQInt8
import com.fpf.smartscansdk.core.processors.ProcessorResult
import com.fpf.smartscansdk.core.processors.BatchProcessor
import com.fpf.smartscansdk.core.processors.Concurrency
import com.fpf.smartscansdk.core.processors.ProcessorListener
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.collections.map

class DeviceIndexer(
    private val embedder: TextEmbeddingProvider,
    private val deviceEmbedStore: EmbeddingStore,
    private val deviceRepository: DeviceRepository,
    private val quantize: Boolean = false,
    concurrency: Concurrency,
    listener: ProcessorListener<DeviceInfo>? = null,
    batchSize: Int = 10,
): BatchProcessor<DeviceInfo, Pair<DeviceInfo, Embedding>>( listener, concurrency, batchSize){

    override suspend fun onBatchComplete(batch: List<Pair<DeviceInfo, Embedding>>) {
        val embedsToStore = batch.map {
            StoredEmbedding(
                it.first.id,
                System.currentTimeMillis(),
                it.second
            )
        }
        // NonCancellable required to avoid file corruption if coroutine cancelled
        withContext(NonCancellable) {
            deviceEmbedStore.add(embedsToStore)
        }
    }

    override suspend fun onProcess(item: DeviceInfo): Pair<DeviceInfo, Embedding> {
        val output = embedder.embed(item.name)
        val embedding = if(quantize) Embedding.QInt8(output.toQInt8()) else Embedding.F32(output)
        return Pair(item, embedding)
    }

    suspend fun index(): ProcessorResult {
        val uniqueDevices = deviceRepository.get()
        val indexedDevices = deviceEmbedStore.get().map { it.id }.toSet()
        val unIndexedDevices = uniqueDevices.filterNot{it.id in indexedDevices }
        return run(unIndexedDevices)
    }
}