package com.fpf.blucon.ui.screens.scan

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.BTDevice
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.bluetooth.BTScanEntry
import com.fpf.blucon.bluetooth.BluetoothDocsYamlParser
import com.fpf.blucon.bluetooth.BluetoothScanner
import com.fpf.blucon.bluetooth.NewBTScan
import com.fpf.blucon.bluetooth.toScan
import com.fpf.blucon.data.MetadataRepository
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
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
    private val metadataRepository: MetadataRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ScanViewModel"
    }


    private val scanner = BluetoothScanner(application)
    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state
    val devices = scanner.devices

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    private val seenDevices: MutableSet<String> = mutableSetOf()

    init {
        viewModelScope.launch {
            devices.collect{ devices ->
                val unseenDevices = devices.filter { it.key !in seenDevices }
                addEntriesFromDevices(unseenDevices.values.toList())
                seenDevices.addAll(unseenDevices.keys)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reset()
                val newBTScan = NewBTScan(longitude = 0.0, latitude = 0.0)
                val scanId = scanRepository.insertScan(newBTScan)
                Log.d(TAG, "scanId= $scanId")
                setScan(newBTScan.toScan(scanId))
                scanner.startScanBle()
                setIsScanning(true)
                setStartTime(System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e(TAG, "Error starting scan", e)
                _event.emit("Error starting scan")
                _state.value.scan?.let{scanRepository.deleteScans(listOf(it))}
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                scanner.stopScanBle()
                setIsScanning(false)
            }catch (e: Exception){
                Log.e(TAG, "Error stopping scan", e)
                _event.emit("Error stopping scan")
            }
        }
    }


    fun getCompanyName(manufacturerId: Int?): String? = metadataRepository.getCompanyName(manufacturerId)

    fun getServiceName(serviceId: Int?): String? = metadataRepository.getServiceName(serviceId)
    private fun setScan(value: BTScan?) = _state.update { it.copy(scan=value) }

    private fun setIsScanning(value: Boolean) = _state.update { it.copy(isScanning = value) }

    private fun setStartTime(value: Long?) = _state.update { it.copy(scanStart = value) }


    private fun reset() {
        _state.update { it.copy(isScanning = false, scan = null, scanStart = null) }
        seenDevices.clear()
        clearDevices()
    }
    fun clearDevices() = scanner.clearDevices()

    private suspend fun addEntriesFromDevices(btDevices: List<BTDevice>){
        val currentState =  _state.value
        val scan = currentState.scan?: return
        val scanStart =currentState.scanStart?: return

        scanEntryRepository.addEntries(
            btDevices.map{
                val manufacturerId = it.manufacturerData.keys.firstOrNull()

                BTScanEntry(
                    scanId = scan.id,
                    timestamp = scanStart,
                    deviceAddress = it.address,
                    rssi = it.rssi,
                    deviceName = it.name,
                    manufacturerId = manufacturerId,
                    manufacturerName = metadataRepository.getCompanyName(manufacturerId)
                )
            }
        )
    }

}
