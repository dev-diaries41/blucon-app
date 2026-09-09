package com.fpf.blucon.ui.screens.scan

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.BTDevice
import com.fpf.blucon.bluetooth.BluetoothDocsYamlParser
import com.fpf.blucon.bluetooth.BluetoothScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ScanViewModel"
    }


    private val scanner = BluetoothScanner(application)
    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state
    val devices = scanner.devices

    val companyIdMap: Map<Int, String> = BluetoothDocsYamlParser.parseCompanyIdentifiers(application, R.raw.bluetooth_company_id)
    val serviceUuidMap: Map<Int, String> = BluetoothDocsYamlParser.parseServiceUuids(application, R.raw.bluetooth_service_uuids)

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    @SuppressLint("MissingPermission")
    fun startScan() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                scanner.startScanBle()
                _state.update { it.copy(isScanning = true) }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting scan", e)
                _event.emit("Error starting scan")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                scanner.stopScanBle()
                _state.update { it.copy(isScanning = false) }
            }catch (e: Exception){
                Log.e(TAG, "Error stopping scan", e)
                _event.emit("Error stopping scan")
            }
        }
    }

    fun clearDevices() = scanner.clearDevices()

    fun setSelectedDevice(device: BTDevice?) = _state.update { it.copy(selectedDevice=device) }
}
