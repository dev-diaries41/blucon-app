package com.fpf.blucon.ui.screens.scan

import com.fpf.blucon.bluetooth.BTDevice
import com.fpf.blucon.bluetooth.BTScan

data class ScanState(
    val isScanning: Boolean = false,
    val scan: BTScan? = null,
    val scanStart: Long? = null,
)