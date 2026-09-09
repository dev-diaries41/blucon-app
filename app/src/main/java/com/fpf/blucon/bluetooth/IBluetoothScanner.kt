package com.fpf.blucon.bluetooth

interface IBluetoothScanner {
    fun startScanBle()
    fun stopScanBle()

    fun startScanClassic()
    fun stopScanClassic()

    fun clearDevices()
}