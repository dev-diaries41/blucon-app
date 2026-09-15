package com.fpf.blucon.di

import com.fpf.blucon.MainViewModel
import com.fpf.blucon.ui.screens.collections.CollectionsViewModel
import com.fpf.blucon.ui.screens.collections.items.CollectionItemsViewModel
import com.fpf.blucon.ui.screens.scan.entries.ScanEntryViewModel
import com.fpf.blucon.ui.screens.history.ScanHistoryViewModel
import com.fpf.blucon.ui.screens.scan.ScanViewModel
import com.fpf.blucon.ui.screens.search.SearchViewModel
import com.fpf.blucon.ui.screens.settings.SettingsViewModel
import com.fpf.blucon.ui.shared.DeviceMetadataViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val viewModelModule = module {
    viewModel {
        ScanViewModel(
            application = get(),
            scanRepository = get(),
            scanEntryRepository = get(),
            locationTracker = get(),
            bluetoothScanner = get()
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
        ScanEntryViewModel(
            application = get(),
            scanEntryRepository = get(),
            sharedPrefs = get()
        )
    }

    viewModel {
        SearchViewModel(
            application = get(),
            scanEntryRepository = get(),
            sharedPrefs = get()
        )
    }

    viewModel {
        DeviceMetadataViewModel(
            application = get(),
            scanEntryRepository = get(),
            deviceClusterRepository = get()
        )
    }

    viewModel {
        SettingsViewModel(
            application = get(),
            scanEntryRepository = get(),
            scanRepository = get(),
            sharedPrefs = get()
        )
    }

    viewModel {
        MainViewModel(
            application = get(),
            sharedPrefs = get()
        )
    }

    viewModel {
        CollectionsViewModel(
            application = get(),
            deviceClusterRepository = get(),
            clusterManager = get()
        )
    }

    viewModel {
        CollectionItemsViewModel(
            application = get(),
            clusterManager = get(),
            deviceRepository = get(),
            deviceClusterRepository = get(),
            sharedPrefs = get()
        )
    }
}
