package com.fpf.blucon.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fpf.blucon.metrics.CountMetric


@Composable
fun <T>CountMetricCard(
    metric: CountMetric<T>,
    itemLabel: String = "items",
    onItemClick: ((T) -> Unit)? = null
) {

    Column(
        modifier = Modifier
            .widthIn(min = 100.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .then(
                if (onItemClick != null) {
                    Modifier.clickable { metric.value?.let{onItemClick(it)} }
                } else {
                    Modifier
                }
            )
            .padding(16.dp)
    ) {
        Text(
            text = metric.label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "${metric.count} $itemLabel",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}