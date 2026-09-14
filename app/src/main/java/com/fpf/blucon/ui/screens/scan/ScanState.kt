package com.fpf.blucon.ui.screens.scan

import android.location.Location
import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.bluetooth.scan.BTScanEntry

data class ScanState(
    val isScanning: Boolean = false,
    val scan: BTScan? = null,
    val devices: Map<String, BTScanEntry> = mapOf(),
    val location: Location? = null
)