package com.fpf.blucon.bluetooth

interface IBluetoothScanner {
    val isBluetoothEnabled: Boolean

    fun startScanBle()
    fun stopScanBle()

    fun startScanClassic()
    fun stopScanClassic()

    fun clearDevices()
}