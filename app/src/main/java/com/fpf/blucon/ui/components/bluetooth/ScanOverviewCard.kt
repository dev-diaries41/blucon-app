package com.fpf.blucon.ui.components.bluetooth

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fpf.blucon.ui.components.common.HorizontalCarouselRow

@Composable
fun ScanOverviewCard(
    manufacturerCounts: Map<String, Int>,
    topK: Int = 6,
    onViewAllManufacturers: () -> Unit
) {
    val sorted = manufacturerCounts.entries
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
            HorizontalCarouselRow(
                label = "Top manufacturers",
                topItemCounts = topManufacturers.associate { it.key to it.value },
                total=sorted.size,
                onViewAll = {onViewAllManufacturers()}
            )
        }
    }
}