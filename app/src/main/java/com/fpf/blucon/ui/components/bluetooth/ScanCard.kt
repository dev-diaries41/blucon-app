package com.fpf.blucon.ui.components.bluetooth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.ui.components.cards.InfoCard
import com.fpf.blucon.ui.components.cards.InfoRow
import com.fpf.blucon.utils.formatDateTime

@Composable
fun ScanCard(
    item: BTScan,
    isSelecting: Boolean,
    isChecked: () -> Boolean,
    onItemClick: (BTScan) -> Unit,
    onItemLongClick: (BTScan) -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoCard(
        modifier = modifier,
        isSelecting = isSelecting,
        isChecked = isChecked,
        onClick = { onItemClick(item) },
        onLongClick = { onItemLongClick(item) },
        content = {
            InfoRow("Scan", "#${item.id}", highlight = true)
            InfoRow("Location", "${item.latitude}, ${item.longitude}")
            InfoRow("Devices", "${item.size}")
            InfoRow("Date", formatDateTime(item.timestamp))
        }
    )
}

