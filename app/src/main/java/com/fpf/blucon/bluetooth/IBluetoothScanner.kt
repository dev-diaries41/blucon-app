package com.fpf.blucon.bluetooth

import kotlinx.coroutines.flow.StateFlow

interface IBluetoothScanner {
    val isBluetoothEnabled: StateFlow<Boolean>

    fun startScanBle()
    fun stopScanBle()

    fun startScanClassic()
    fun stopScanClassic()

    fun clearDevices()
}