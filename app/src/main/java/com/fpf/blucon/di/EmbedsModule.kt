package com.fpf.blucon.di

import android.app.Application
import com.fpf.blucon.embeds.EmbeddingStoresFiles
import com.fpf.smartscansdk.core.embeddings.FileEmbeddingStore
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

private const val MINILM_EMBEDDING_DIM = 384

val DEVICE_GROUPS_EMBED_STORE = named("device_groups_embed_store")

val embedsModule = module {
    single(DEVICE_GROUPS_EMBED_STORE) {
        val app = get<Application>()
        FileEmbeddingStore(File(app.filesDir, EmbeddingStoresFiles.DEVICE_GROUPS), MINILM_EMBEDDING_DIM, quantize = true)
    }
}