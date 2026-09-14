package com.fpf.blucon.ui.components.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fpf.blucon.bluetooth.scan.BTScanEntry

@Composable
fun DeviceList(
    devices: List<BTScanEntry>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(
            items = devices,
            key = { it.deviceAddress }
        ) { device ->
            ScanEntryCard(
                item = device,
            )
        }
    }
}
