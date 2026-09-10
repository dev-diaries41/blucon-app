package com.fpf.blucon.ui.components.bluetooth

import androidx.compose.runtime.Composable
import com.fpf.blucon.bluetooth.BTScanEntry


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fpf.blucon.ui.components.common.CircularCheckbox
import com.fpf.blucon.utils.formatDateTime


@Composable
fun ScanEntryCard(
    item: BTScanEntry,
    modifier: Modifier = Modifier,
    isSelecting: Boolean = false,
    isChecked: (() -> Boolean)? = null,
    onItemClick: ((BTScanEntry) -> Unit)? = null,
    onItemLongClick: ((BTScanEntry) -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val manufacturer: String? = item.manufacturerName?: item.manufacturerId?.let{it.toString()}
    Box(
        modifier = modifier
            .heightIn(max = 216.dp)
            .padding(4.dp)
            .clip(shape)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape
            )
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onItemClick?.invoke(item) },
                onLongClick = { onItemLongClick?.invoke(item) }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.deviceName ?: "Unknown device",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier.weight(1f))

                Text(
                    text = "Scan #${item.scanId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = item.deviceAddress,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "RSSI: ${item.rssi} dBm",
                style = MaterialTheme.typography.bodyMedium
            )

            item.manufacturerId?.let {
                Text(
                    text = "Manufacturer: $manufacturer",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (isSelecting) {
            CircularCheckbox(
                checked = isChecked?.invoke()?: false,
                onCheckedChange = { onItemClick?.invoke(item) },
                modifier = Modifier
                    .offset(x = 8.dp, y = 8.dp)
                    .align(Alignment.TopStart)
            )
        }
    }
}