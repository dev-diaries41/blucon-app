package com.fpf.blucon.ui.screens.scan

import android.widget.Toast
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
import com.fpf.blucon.ui.components.bluetooth.ScanResultList
import com.fpf.blucon.ui.permissions.RequestPermissions
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fpf.blucon.ui.components.common.LoadingIndicator
import com.fpf.blucon.ui.components.placeholders.EmptyItemsScreen


@Composable
fun ScanScreen(
    onScan: () -> Unit,
    onStopScan: () -> Unit,
    onTopBarChange: (TopBarState) -> Unit,
    onViewScanHistory: () -> Unit,
    onViewSettings: () -> Unit,
    viewModel: ScanViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var bluetoothGranted by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }
    val screenTitle = stringResource(R.string.title_scan)
    val devices = state.devices.values.toList()
    val isBluetoothScanning by viewModel.isBluetoothScanning.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val location by viewModel.location.collectAsState()
    val isScanning = isBluetoothScanning && isTracking
    val isBluetoothEnabled by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
    val isLocationEnabled by viewModel.isLocationEnabled.collectAsStateWithLifecycle()
    val isScanEnabled = isBluetoothEnabled && isLocationEnabled
    val isFindingLocation = location == null && isTracking && !isBluetoothScanning

    val emptyScreenTitle = when{
        isFindingLocation  -> "Finding location"
        isScanning -> "Scanning devices"
        else  -> "Scan devices"
    }

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = screenTitle,
                actions = {
                    IconButton (onClick = { onViewScanHistory()}) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "scan history"
                        )
                    }
                    IconButton(onClick = onViewSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT ).show()
        }
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
                if(isScanning || isFindingLocation){
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
            title = emptyScreenTitle ,
            description = if(isScanning) "No devices found" else  "",
            isVisible = devices.isEmpty()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if(isScanning && devices.isNotEmpty()){
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

            if (!isLocationEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text("Location is off", color = MaterialTheme.colorScheme.error)
                }
            }

            if (!isBluetoothEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.BluetoothDisabled,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text("Bluetooth is off", color = MaterialTheme.colorScheme.error)
                }
            }


            ScanResultList(
                devices = devices,
                modifier = Modifier.weight(1f),
            )

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isScanning) {
                            onStopScan()
                        } else {
                            onScan()
                            viewModel.startScan()
                        }
                    },
                    enabled = bluetoothGranted && locationGranted && isScanEnabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isScanning || isFindingLocation) "Stop scan" else "Scan")
                }

                Button(
                    onClick = viewModel::clearDevices,
                    enabled = !isScanning && state.devices.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }
    }
}