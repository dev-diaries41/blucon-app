package com.fpf.blucon.ui.screens.scan

import android.location.Location
import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.bluetooth.scan.BTScanEntry
import com.fpf.blucon.bluetooth.scan.BluetoothScanResult

data class ScanState(
    val devices: Map<String, BluetoothScanResult> = mapOf(),
)