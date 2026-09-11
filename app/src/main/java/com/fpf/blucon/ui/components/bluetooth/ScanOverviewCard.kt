package com.fpf.blucon.ui.components.bluetooth

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ScanOverviewCard(
    counts: Map<String, Int>,
    topK: Int = 6,
    onViewAllManufacturers: () -> Unit
) {
    val sorted = counts.entries
        .filter { it.value > 1 }
        .sortedByDescending { it.value }

    if (sorted.isEmpty()) return

    val topManufacturers = sorted.take(topK)


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
                label = "Top manufacturers",
                topManufacturers = topManufacturers,
                total=sorted.size,
                onViewAllManufacturers = {onViewAllManufacturers()}
            )
        }
    }
}

@Composable
private fun ManufacturerSummary(
    label: String,
    topManufacturers: List<Map.Entry<String, Int>>,
    total: Int,
    onViewAllManufacturers: () -> Unit
) {
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

           if(total > topManufacturers.size){
               TextButton(
                   onClick = onViewAllManufacturers,
               ) {
                   Text("See all", style = MaterialTheme.typography.bodyMedium)
               }
           }
       }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            topManufacturers.forEach { (manufacturer, count) ->
                Column(
                    modifier = Modifier.widthIn(min = 100.dp)
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