package com.fpf.blucon.ui.screens.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpf.blucon.ui.components.bluetooth.DeviceList
import com.fpf.blucon.ui.permissions.RequestPermissions

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val companyIdMap = viewModel.companyIdMap
    val serviceUuidMap = viewModel.serviceUuidMap
    var bluetoothGranted by remember { mutableStateOf(false) }

    RequestPermissions { _, bluetoothOk ->
        bluetoothGranted = bluetoothOk
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nearby devices",
                style = MaterialTheme.typography.headlineSmall
            )

            if (state.isScanning) {
                Text(
                    text = "Scanning...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            DeviceList(
                devices = devices.values.toList(),
                companyIdMap=companyIdMap,
                serviceUuidMap=serviceUuidMap,
                modifier = Modifier.weight(1f),
            )

            if (!bluetoothGranted) {
                Text(
                    text = "Bluetooth permission is required",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (state.isScanning) {
                            viewModel.stopScan()
                        } else {
                            viewModel.startScan()
                        }
                    },
                    enabled = bluetoothGranted,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isScanning) "Stop scan" else "Scan")
                }

                Button(
                    onClick = viewModel::clearDevices,
                    enabled = !state.isScanning && devices.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }
    }
}