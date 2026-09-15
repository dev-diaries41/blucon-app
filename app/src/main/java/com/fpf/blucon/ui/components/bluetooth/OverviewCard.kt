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
import com.fpf.blucon.metrics.CountMetric
import com.fpf.blucon.ui.components.common.CountMetricCard
import com.fpf.blucon.ui.components.common.HorizontalCarouselRow
import com.fpf.blucon.ui.components.search.Header

@Composable
fun OverviewCard(
    topManufacturerCounts: List<CountMetric<Nothing>>,
    topDeviceNameCounts:List<CountMetric<Nothing>>,
    topCollectionCounts: List<CountMetric<DeviceCollection>>,
    onViewAllManufacturers: (() -> Unit)? = null,
    onViewAllDevices: (() -> Unit)? = null,
    onViewCollections: (() -> Unit)? = null,
    onCollectionClick: ((DeviceCollection) -> Unit)? = null,
    headerContent: (@Composable () -> Unit)?=null
) {
    val topK = maxOf(topManufacturerCounts.size, topDeviceNameCounts.size)

    val sortedManufacturers = topManufacturerCounts.sortedByDescending { it.count }
    val sortedDevices = topDeviceNameCounts.sortedByDescending { it.count }
    val sortedCollections = topCollectionCounts.sortedByDescending { it.count }

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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            headerContent?.invoke()
            HorizontalCarouselRow(
                label = "Top manufacturers",
                topItemCounts = topManufacturers,
                onViewAll = onViewAllManufacturers
            ){
                CountMetricCard(
                    metric = it,
                    onItemClick = onCollectionClick
                )
            }

            HorizontalCarouselRow(
                label = "Top device names",
                topItemCounts = topDevices,
                onViewAll = onViewAllDevices
            ){
                CountMetricCard(
                    metric = it,
                    onItemClick = onCollectionClick
                )
            }
            HorizontalCarouselRow(
                label = "Top collections",
                topItemCounts = topCollections,
                onViewAll = onViewCollections,
            ){
                CountMetricCard(
                    metric = it,
                    onItemClick = onCollectionClick
                )
            }
        }
    }
}