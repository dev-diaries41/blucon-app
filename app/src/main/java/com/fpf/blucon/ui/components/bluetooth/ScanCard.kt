package com.fpf.blucon.ui.components.bluetooth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.ui.components.common.CircularCheckbox
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
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .heightIn(max = 216.dp)
            .padding(4.dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScanRow("Scan", "#${item.id}", highlight = true)
            ScanRow("Location", "${item.latitude}, ${item.longitude}")
            ScanRow("Devices", "${item.size}")
            ScanRow("Date", formatDateTime(item.timestamp))
        }

        if (isSelecting) {
            CircularCheckbox(
                checked = isChecked(),
                onCheckedChange = { onItemClick(item) },
                modifier = Modifier
                    .offset(x = 8.dp, y = 8.dp)
                    .align(Alignment.TopStart)
            )
        }
    }
}

@Composable
private fun ScanRow(label: String, value: String, highlight: Boolean = false) {
    val color = if(highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.weight(0.35f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            textAlign = TextAlign.End,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.65f),
            color=color
        )
    }
}