package com.fpf.blucon.ui.components.bluetooth


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.ui.components.cards.InfoCard
import com.fpf.blucon.ui.components.cards.InfoRow

@Composable
fun ScanEntryCard(
    item: BTScanEntry,
    modifier: Modifier = Modifier,
    isSelecting: Boolean = false,
    isChecked: (() -> Boolean)? = null,
    onItemClick: ((BTScanEntry) -> Unit)? = null,
    onItemLongClick: ((BTScanEntry) -> Unit)? = null,
) {

    val manufacturer = item.manufacturerName ?: item.manufacturerId?.toString()

    InfoCard(
        modifier = modifier,
        isSelecting = isSelecting,
        isChecked = { isChecked?.invoke() ?: false },
        onClick = { onItemClick?.invoke(item) },
        onLongClick = { onItemLongClick?.invoke(item) },
        content = {
            InfoRow("Device", item.deviceName ?: "Unknown device", highlight = true)
            InfoRow("Address", item.deviceAddress)
            InfoRow("RSSI", "${item.rssi} dBm")
            manufacturer?.let { InfoRow("Manufacturer", it) }
        }
    )
}