package com.fpf.blucon.ui.components.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fpf.blucon.bluetooth.scan.BluetoothScanResult

@Composable
fun ScanResultList(
    devices: List<BluetoothScanResult>,
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
            BluetoothScanResultCard(
                item = device,
            )
        }
    }
}
