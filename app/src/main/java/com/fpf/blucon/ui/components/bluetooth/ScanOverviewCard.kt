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
    topManufacturerCounts: Map<String, Int>,
    topDeviceNameCounts: Map<String, Int>,
    onViewAllManufacturers: (() -> Unit)? = null,
    onViewAllDevices: (() -> Unit)? = null
) {
    val topK = maxOf(topManufacturerCounts.size, topDeviceNameCounts.size)

    val sortedManufacturers = topManufacturerCounts.entries
        .sortedByDescending { it.value }

    val sortedDevices = topDeviceNameCounts.entries
        .sortedByDescending { it.value }

    if (sortedManufacturers.isEmpty()) return
    if (sortedDevices.isEmpty()) return

    val topDevices = sortedDevices.take(topK)
    val topManufacturers = sortedManufacturers.take(topK)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = MaterialTheme.shapes.medium,
//        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HorizontalCarouselRow(
                label = "Top manufacturers",
                topItemCounts = topManufacturers.associate { it.key to it.value },
                onViewAll = onViewAllManufacturers
            )
            HorizontalCarouselRow(
                label = "Top device names",
                topItemCounts = topDevices.associate { it.key to it.value },
                onViewAll = onViewAllDevices
            )
        }
    }
}