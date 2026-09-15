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
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.ui.components.common.HorizontalCarouselRow

@Composable
fun ScanOverviewCard(
    topManufacturerCounts:List<Triple<String, Nothing?,  Int>>, //name, value, count
    topDeviceNameCounts: List<Triple<String, Nothing?,  Int>>, //name, value, count
    topCollectionCounts: List<Triple<String, DeviceCollection,  Int>>, //name, value, count
    onViewAllManufacturers: (() -> Unit)? = null,
    onViewAllDevices: (() -> Unit)? = null,
    onViewCollections: (() -> Unit)? = null,
    onCollectionClick: ((DeviceCollection) -> Unit)? = null
) {
    val topK = maxOf(topManufacturerCounts.size, topDeviceNameCounts.size)
    val sortedManufacturers = topManufacturerCounts.sortedByDescending { it.third }
    val sortedDevices = topDeviceNameCounts.sortedByDescending { it.third }
    val sortedCollections = topCollectionCounts.sortedByDescending { it.third }

    if (sortedManufacturers.isEmpty()) return
    if (sortedDevices.isEmpty()) return

    val topDevices = sortedDevices.take(topK)
    val topManufacturers = sortedManufacturers.take(topK)
    val topCollections = sortedCollections.take(topK)

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
                topItemCounts = topManufacturers,
                onViewAll = onViewAllManufacturers
            )
            HorizontalCarouselRow(
                label = "Top device names",
                topItemCounts = topDevices,
                onViewAll = onViewAllDevices
            )
            HorizontalCarouselRow(
                label = "Top collections",
                topItemCounts = topCollections,
                onViewAll = onViewCollections,
                onItemClick = onCollectionClick
            )
        }
    }
}