package com.fpf.blucon.di

import com.fpf.blucon.ui.screens.devices.DevicesViewModel
import com.fpf.blucon.ui.screens.history.ScanHistoryViewModel
import com.fpf.blucon.ui.screens.scan.ScanViewModel
import com.fpf.blucon.ui.screens.search.SearchViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val viewModelModule = module {
    viewModel {
        ScanViewModel(
            application = get(),
            scanRepository = get(),
            scanEntryRepository = get(),
            metadataRepository = get()
        )
    }


    viewModel {
        ScanHistoryViewModel(
            application = get(),
            scanRepository = get(),
            sharedPrefs = get(),
        )
    }

    viewModel {
        DevicesViewModel(
            application = get(),
            scanEntryRepository = get(),
            sharedPrefs = get()
        )
    }

    viewModel {
        SearchViewModel(
            application = get(),
            scanEntryRepository = get(),
            metadataRepository = get(),
            sharedPrefs = get()
        )
    }
}
