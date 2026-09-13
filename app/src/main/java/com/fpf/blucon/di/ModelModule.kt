package com.fpf.blucon.di

import com.fpf.blucon.R
import com.fpf.smartscansdk.ml.embeddings.minilm.MiniLMTextEmbedder
import com.fpf.smartscansdk.ml.models.ModelAssetSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import com.fpf.smartscansdk.core.embeddings.TextEmbeddingProvider

val modelModule = module {
    single<TextEmbeddingProvider> {
        val resources = androidContext().resources
        val modelSource = ModelAssetSource.Resource(resources, R.raw.minilm_sentence_transformer_quant)
        val vocabSource = ModelAssetSource.Resource(resources, R.raw.vocab)
        val configSource = ModelAssetSource.Resource(resources, R.raw.config)

        MiniLMTextEmbedder(
            modelSource = modelSource,
            vocabSource=vocabSource,
            configSource=configSource
        )
    }
}