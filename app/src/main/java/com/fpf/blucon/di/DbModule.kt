package com.fpf.blucon.di

import com.fpf.blucon.data.MetadataRepository
import com.fpf.blucon.data.ScanDatabase
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val dbModule = module {
    single {
        ScanDatabase.getDatabase(androidApplication())
    }
    single { get<ScanDatabase>().scanDao() }
    single { get<ScanDatabase>().scanEntryDao() }
    single { ScanRepository(get(), get()) }
    single{ MetadataRepository(get()) }
    single { ScanEntryRepository(dao = get(), metadataRepository = get()) } // TODO: rename metadataRepository
}