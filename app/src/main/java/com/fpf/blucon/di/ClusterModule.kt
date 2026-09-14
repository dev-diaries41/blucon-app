package com.fpf.blucon.di

import com.fpf.blucon.cluster.ClusterManager
import org.koin.dsl.module


val clusterModule = module {
    single {
        ClusterManager(
            clusterEmbedStore = get(DEVICE_CLUSTERS_EMBED_STORE),
            deviceEmbedStore = get(DEVICE_EMBED_STORE),
            clusterRepository = get(),
            clusterCrossRefRepository = get(),
        )
    }
}
