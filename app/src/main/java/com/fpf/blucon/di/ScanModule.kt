package com.fpf.blucon.di

import com.fpf.blucon.bluetooth.scan.BluetoothScanner
import com.fpf.blucon.location.LocationTracker
import org.koin.dsl.module

val scanModule = module {
    single {
        LocationTracker(get())
    }
    single {
        BluetoothScanner(get())
    }
}