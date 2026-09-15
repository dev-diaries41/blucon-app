package com.fpf.blucon.ui.screens.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.blucon.bluetooth.scan.BluetoothScanResult
import com.fpf.blucon.bluetooth.scan.BluetoothScanner
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
import com.fpf.blucon.location.LocationTracker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanViewModel(
    application: Application,
    private val scanRepository: ScanRepository,
    private val scanEntryRepository: ScanEntryRepository,
    private val bluetoothScanner: BluetoothScanner,
    private val locationTracker: LocationTracker
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ScanViewModel"
    }

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    val isLocationEnabled: StateFlow<Boolean> = locationTracker.isLocationEnabled

    val isBluetoothEnabled: StateFlow<Boolean> = bluetoothScanner.isBluetoothEnabled

    val isBluetoothScanning: StateFlow<Boolean> = bluetoothScanner.isScanning

    val isTracking: StateFlow<Boolean> = locationTracker.isTracking

    val location: StateFlow<Location?> = locationTracker.location

    init {
        observeDevices()
    }

    private fun observeDevices() {
        viewModelScope.launch {
            bluetoothScanner.devices.collect { devices ->
                _state.update {
                    it.copy(devices = formatDevices(devices))
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun startScan() {
        clearDevices()
    }

    fun clearDevices() {
        bluetoothScanner.clearDevices()
        _state.update { it.copy(devices = mapOf()) }
    }

    private fun formatDevices(devices: Map<String, BluetoothScanResult>): Map<String, BluetoothScanResult> {
        return devices.mapValues { (_, device) ->
            device.copy(
                manufacturerName = device.manufacturerId?.let {
                    scanEntryRepository.getCompanyName(it)
                }
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onCleared() {
        locationTracker.stop()
        bluetoothScanner.stopScanBle()
        super.onCleared()
    }
}