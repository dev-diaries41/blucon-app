package com.fpf.blucon.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fpf.blucon.metrics.CountMetric


@Composable
fun <T>HorizontalCarouselRow(
    label: String,
    topItemCounts: List<CountMetric<T>>,
    onViewAll: (() -> Unit)? = null,
    itemContent: @Composable (CountMetric<T>) -> Unit
) {
    val isEmpty = topItemCounts.isEmpty()
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isEmpty && onViewAll != null) {
                TextButton(
                    onClick = onViewAll,
                ) {
                    Text("See all", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (isEmpty) {
            Text("No items", style = MaterialTheme.typography.bodySmall)
        } else {

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                topItemCounts.forEach { metric ->
                    itemContent(metric)
                }
            }
        }
    }
}