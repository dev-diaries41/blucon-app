package com.fpf.blucon.bluetooth.scan

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.util.size
import com.fpf.blucon.errors.AppException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class BluetoothScanner(
    private val context: Context,
) : IBluetoothScanner {

    companion object {
        private const val TAG = "BluetoothScanner"
    }

    private val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter
    private var bleScanCallback: ScanCallback? = null
    private var classicReceiver: BroadcastReceiver? = null

    private val _devices = MutableStateFlow<Map<String, BluetoothScanResult>>(emptyMap())
    val devices: StateFlow<Map<String, BluetoothScanResult>> = _devices


    private val _isBluetoothEnabled = MutableStateFlow(
        bluetoothAdapter?.isEnabled == true
    )
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return

            when (intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR
            )) {
                BluetoothAdapter.STATE_ON -> _isBluetoothEnabled.value = true
                BluetoothAdapter.STATE_OFF -> _isBluetoothEnabled.value = false
            }
        }
    }

    init{
        ContextCompat.registerReceiver(
            context,
            bluetoothStateReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun startScanBle() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
        if(!_isBluetoothEnabled.value) throw AppException.BluetoothUnavailableException()

        val scanner = bluetoothAdapter.bluetoothLeScanner ?: return

        if (bleScanCallback != null) return

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val record = result.scanRecord ?: return

                val bluetoothDevice = BluetoothScanResult(
                    deviceName = record.deviceName,
                    deviceAddress = result.device.address,
                    rssi = result.rssi,
                    serviceUuids = record.serviceUuids?.map { it.uuid } ?: emptyList(),
                    manufacturerData = buildMap {
                        for (i in 0 until record.manufacturerSpecificData.size) {
                            put(
                                record.manufacturerSpecificData.keyAt(i),
                                record.manufacturerSpecificData.valueAt(i)
                            )
                        }
                    },
                    serviceData = buildMap {
                        record.serviceData.forEach { (uuid, data) ->
                            put(uuid.uuid, data)
                        }
                    },
                    txPower = record.txPowerLevel.takeUnless { it == Int.MIN_VALUE }
                )

                _devices.update { it + (bluetoothDevice.deviceAddress to bluetoothDevice) }
            }

            override fun onScanFailed(errorCode: Int) {
                bleScanCallback = null
            }
        }

        bleScanCallback = callback
        scanner.startScan(callback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopScanBle() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
        val callback = bleScanCallback ?: return
        bluetoothAdapter.bluetoothLeScanner?.stopScan(callback)
        bleScanCallback = null
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun startScanClassic() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN) ||
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        ) return
        if(!_isBluetoothEnabled.value) throw AppException.BluetoothUnavailableException()

        if (classicReceiver != null) return

        val receiver = object : BroadcastReceiver() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != BluetoothDevice.ACTION_FOUND) return

                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return

                val bluetoothDevice = BluetoothScanResult(
                    deviceName = device.name,
                    deviceAddress = device.address,
                    rssi = intent.getShortExtra(
                        BluetoothDevice.EXTRA_RSSI,
                        Short.MIN_VALUE
                    ).toInt()
                )

                _devices.update { it + (bluetoothDevice.deviceAddress to bluetoothDevice) }
            }
        }

        classicReceiver = receiver

        context.registerReceiver(
            receiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        bluetoothAdapter.startDiscovery()
    }

    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    ])
    override fun stopScanClassic() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN) ||
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        ) return

        classicReceiver?.let {
            context.unregisterReceiver(it)
            classicReceiver = null
        }

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    override fun clearDevices() {
        _devices.update { emptyMap() }
    }

    private fun hasPermission(permission: String): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}