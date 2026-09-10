package com.fpf.blucon.ui.screens.scan

import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.BTScanEntry

data class ScanState(
    val isScanning: Boolean = false,
    val scan: BTScan? = null,
    val startTime: Long? = null,
    val devices: Map<String, BTScanEntry> = mapOf()
)