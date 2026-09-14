package com.fpf.blucon.ui.components.bluetooth


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fpf.blucon.bluetooth.scan.BluetoothScanResult
import com.fpf.blucon.ui.components.cards.InfoCard
import com.fpf.blucon.ui.components.cards.InfoText

@Composable
fun BluetoothScanResultCard(
    item: BluetoothScanResult,
    modifier: Modifier = Modifier,
) {

    val manufacturer = item.manufacturerName ?: item.manufacturerId?.toString()

    InfoCard(
        modifier = modifier,
        content = {
            InfoText( item.deviceName ?: "Unknown device", highlight = true)
            InfoText(item.deviceAddress)
            InfoText("${item.rssi} dBm")
            manufacturer?.let { InfoText(it) }
        }
    )
}