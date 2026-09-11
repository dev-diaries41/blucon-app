package com.fpf.blucon.ui.components.bluetooth


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.ui.components.cards.InfoCard
import com.fpf.blucon.ui.components.cards.InfoRow
import com.fpf.blucon.ui.components.cards.InfoText

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
            InfoText( item.deviceName ?: "Unknown device", highlight = true)
            InfoText(item.deviceAddress)
            InfoText("${item.rssi} dBm")
            manufacturer?.let { InfoText(it) }
        }
    )
}