package com.fpf.blucon.ui.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RequestPermissions(
    onPermissionsResult: (notificationGranted: Boolean, bluetoothGranted: Boolean, locationGranted: Boolean) -> Unit
) {
    val permissionsToRequest = mutableListOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        } else {
            true
        }

        val bluetoothGranted =
            permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                    permissions[Manifest.permission.BLUETOOTH_CONNECT] == true

        val locationGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        onPermissionsResult(
            notificationGranted,
            bluetoothGranted,
            locationGranted
        )
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}