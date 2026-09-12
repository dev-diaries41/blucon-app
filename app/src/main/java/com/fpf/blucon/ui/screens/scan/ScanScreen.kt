package com.fpf.blucon.ui.screens.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpf.blucon.R
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.components.bluetooth.DeviceList
import com.fpf.blucon.ui.permissions.RequestPermissions
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import com.fpf.blucon.ui.components.common.LoadingIndicator
import com.fpf.blucon.ui.components.placeholders.EmptyItemsScreen


@Composable
fun ScanScreen(
    onTopBarChange: (TopBarState) -> Unit,
    onViewScanHistory: () -> Unit,
    onSearch: () -> Unit,
    viewModel: ScanViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var bluetoothGranted by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }
    val screenTitle = stringResource(R.string.title_scan)
    val devices = state.devices.values.toList()

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = screenTitle,
                actions = {
                    IconButton (onClick = { onSearch()}) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "search devices"
                        )
                    }
                    IconButton (onClick = { onViewScanHistory()}) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "scan history"
                        )
                    }
                }
            )
        )
    }

    RequestPermissions { _, bluetoothOk, locationOk ->
        bluetoothGranted = bluetoothOk
        locationGranted =  locationOk
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        EmptyItemsScreen(
            icon = {
                if(state.isScanning){
                    LoadingIndicator(isVisible = true, size = 64.dp)
                }else{
                    Icon(
                        imageVector = Icons.Filled.Scanner,
                        contentDescription = "Scanner icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(96.dp)
                    )
                }
            },
            title = if(state.isScanning) "Scanning devices" else  "Scan devices" ,
            description = if(state.isScanning) "No devices found" else  "",
            isVisible = devices.isEmpty()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if(state.isScanning && devices.isNotEmpty()){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scanning...",
                    )

                    Spacer( modifier = Modifier.weight(1f))
                    LoadingIndicator(isVisible = true, size = 24.dp)
                }
            }
            DeviceList(
                devices = devices,
                modifier = Modifier.weight(1f),
            )

            if (!bluetoothGranted) {
                Text(
                    text = "Bluetooth permission is required",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (!locationGranted) {
                Text(
                    text = "Location permission is required",
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
                    enabled = !state.isScanning && state.devices.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }
    }
}