package com.fpf.blucon.ui.screens.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.blucon.bluetooth.BTDevice
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.bluetooth.BluetoothScanner
import com.fpf.blucon.bluetooth.NewBTScan
import com.fpf.blucon.bluetooth.toScan
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
import com.fpf.blucon.location.LocationTracker
import kotlinx.coroutines.Dispatchers
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
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ScanViewModel"
    }

    private val scanner = BluetoothScanner(application)
    private val locationTracker = LocationTracker(application)

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            scanner.devices.collect { devices ->
                val unseenDevices = devices.filter { it.key !in _state.value.devices.keys }
                val entries = toScanEntries(unseenDevices.values.toList())

                _state.update {
                    it.copy(
                        devices = (it.devices.values + entries)
                            .associateBy { device -> device.deviceAddress }
                    )
                }

                scanEntryRepository.addEntries(entries)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reset()

                val location = locationTracker.start()

                val newBTScan = NewBTScan(
                    longitude = location.longitude,
                    latitude = location.latitude
                )

                val scanId = scanRepository.insertScan(newBTScan)
                Log.d(TAG, "scanId=$scanId, lat=${location.latitude}, lon=${location.longitude}")

                setScan(newBTScan.toScan(scanId))
                scanner.startScanBle()
                setIsScanning(true)
                setStartTime(System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e(TAG, "Error starting scan", e)
                locationTracker.stop()
                _event.emit(e.message ?: "Error starting scan")

                _state.value.scan?.let {
                    scanRepository.deleteScans(listOf(it))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                scanner.stopScanBle()
                locationTracker.stop()
                setIsScanning(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping scan", e)
                _event.emit("Error stopping scan")
            }
        }
    }

    private fun setScan(value: BTScan?) = _state.update { it.copy(scan = value) }

    private fun setIsScanning(value: Boolean) = _state.update { it.copy(isScanning = value) }

    private fun setStartTime(value: Long?) = _state.update { it.copy(startTime = value) }

    private fun reset() {
        locationTracker.stop()
        _state.update {
            it.copy(
                isScanning = false,
                scan = null,
                startTime = null,
                devices = mapOf()
            )
        }
        clearDevices()
    }

    fun clearDevices() {
        scanner.clearDevices()
        _state.update { it.copy(devices = mapOf()) }
    }

    private fun toScanEntries(btDevices: List<BTDevice>): List<BTScanEntry> {
        val currentState = _state.value
        val scan = currentState.scan ?: return emptyList()
        val scanStart = currentState.startTime ?: return emptyList()

        return btDevices.map {
            val manufacturerId = it.manufacturerData.keys.firstOrNull()

            BTScanEntry(
                scanId = scan.id,
                timestamp = scanStart,
                deviceAddress = it.address,
                rssi = it.rssi,
                deviceName = it.name,
                manufacturerId = manufacturerId,
                manufacturerName = scanEntryRepository.getCompanyName(manufacturerId)
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onCleared() {
        locationTracker.stop()
        scanner.stopScanBle()
        super.onCleared()
    }
}