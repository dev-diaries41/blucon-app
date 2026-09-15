package com.fpf.blucon.ui.components.collections
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fpf.blucon.ui.components.cards.InfoCard
import com.fpf.blucon.bluetooth.device.DeviceCollection


@Composable
fun DeviceCollectionCard(
    collection: DeviceCollection,
    isSelecting: Boolean = false,
    isChecked: ((DeviceCollection) -> Boolean)? = null,
    onItemClick: ((DeviceCollection) -> Unit)? = null,
    onItemLongClick: ((DeviceCollection) -> Unit)? = null,
) {

    InfoCard(
        isSelecting=isSelecting,
        isChecked = { isChecked?.invoke(collection)?: false},
        onClick = { onItemClick?.invoke(collection) },
        onLongClick = { onItemLongClick?.invoke(collection) },
        content = {
            Column(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(4.dp)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${collection.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
