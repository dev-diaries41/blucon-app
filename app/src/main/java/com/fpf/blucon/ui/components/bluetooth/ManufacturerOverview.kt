package com.fpf.blucon.ui.components.bluetooth

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ManufacturerOverview(
    counts: Map<String, Int>,
    topK: Int = 3
) {
    val sorted = counts.entries
        .filter { it.value > 1 }
        .sortedByDescending { it.value }

    if (sorted.isEmpty()) return

    val mostCommon = sorted.take(topK)

    val rarest = if (sorted.size > topK) {
        sorted.drop(topK).sortedBy { it.value }.take(topK)
    } else {
        emptyList()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = MaterialTheme.shapes.medium,
//        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ManufacturerSummary(
                label = "Common manufacturers",
                manufacturers = mostCommon
            )

            if (rarest.isNotEmpty()) {
                ManufacturerSummary(
                    label = "Rare manufacturers",
                    manufacturers = rarest
                )
            }
        }
    }
}

@Composable
private fun ManufacturerSummary(
    label: String,
    manufacturers: List<Map.Entry<String, Int>>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            manufacturers.forEach { (manufacturer, count) ->
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = manufacturer,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "$count devices",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}