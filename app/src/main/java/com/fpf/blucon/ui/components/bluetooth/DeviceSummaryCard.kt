package com.fpf.blucon.ui.components.bluetooth


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fpf.blucon.bluetooth.DeviceSummary
import com.fpf.blucon.ui.components.cards.InfoCard
import com.fpf.blucon.ui.components.cards.InfoText
import com.fpf.blucon.utils.formatDateTime

@Composable
fun DeviceSummaryCard(
    item: DeviceSummary,
    modifier: Modifier = Modifier,
    isSelecting: Boolean = false,
    isChecked: (() -> Boolean)? = null,
    onItemClick: ((DeviceSummary) -> Unit)? = null,
    onItemLongClick: ((DeviceSummary) -> Unit)? = null,
) {
    val manufacturer = item.manufacturerName ?: item.manufacturerId?.toString()

    InfoCard(
        modifier = modifier,
        isSelecting = isSelecting,
        isChecked = { isChecked?.invoke() ?: false },
        onClick = { onItemClick?.invoke(item) },
        onLongClick = { onItemLongClick?.invoke(item) },
        content = {
            InfoText(item.deviceName ?: "Unknown device", highlight = true)
            InfoText(
                "${manufacturer ?: "Unknown manufacturer"} · ${item.rssi} dBm"
            )
            InfoText(item.deviceAddress)
            InfoText("Last seen ${formatDateTime(item.lastSeen)} · ${item.scanCount} scans")
        }
    )
}