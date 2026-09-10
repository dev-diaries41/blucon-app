package com.fpf.blucon.di

import com.fpf.blucon.ui.screens.scan.ScanViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val viewModelModule = module {
    viewModel {
        ScanViewModel(
            application = get()
        )
    }
}
