package com.fpf.blucon.ui.screens.scan

import com.fpf.blucon.bluetooth.BTDevice

data class ScanState(
    val isScanning: Boolean = false,
    val selectedDevice: BTDevice? = null
)