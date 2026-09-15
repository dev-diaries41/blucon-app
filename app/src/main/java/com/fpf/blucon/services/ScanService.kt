package com.fpf.blucon.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.fpf.blucon.MainActivity
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.scan.BluetoothScanResult
import com.fpf.blucon.bluetooth.scan.BluetoothScanner
import com.fpf.blucon.bluetooth.scan.BTScanEntry
import com.fpf.blucon.bluetooth.scan.NewBTScan
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
import com.fpf.blucon.errors.AppException
import com.fpf.blucon.location.LocationTracker
import com.fpf.blucon.notifications.NotificationChannels
import com.fpf.blucon.notifications.showNotification
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class ScanService : Service(), KoinComponent {

    companion object {
        private const val NOTIFICATION_ID = 301
        private const val TAG = "ScanService"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Default)

    private val locationTracker: LocationTracker by inject()
    private val bluetoothScanner: BluetoothScanner by inject()
    private val scanRepository: ScanRepository by inject()
    private val scanEntryRepository: ScanEntryRepository by inject()

    private var currentScanId: Long? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        observeLocation()
        observeDevices()
    }

    private fun startForegroundServiceNotification() {
        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            this,
            NotificationChannels.SCAN_SERVICE
        )
            .setContentTitle("Scanning devices")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(activityPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startScan()
        return START_NOT_STICKY
    }

    private fun startScan() {
        serviceScope.launch {
            try {
                locationTracker.start()
            } catch (e: AppException.LocationUnavailableException) {
                Log.e(TAG, "Error starting location tracker", e)
                handleServiceError(e)
                stopScan()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun observeLocation() {
        serviceScope.launch {
            locationTracker.location.collectLatest { location ->

                if (location == null || currentScanId != null) return@collectLatest

                try {
                    startBluetoothScan(location)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting bluetooth scan", e)
                    handleServiceError(e)
                    stopScan()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun startBluetoothScan(location: Location) {
        if (currentScanId != null) return

        val newBTScan = NewBTScan(
            longitude = location.longitude,
            latitude = location.latitude
        )

        try {
            val scanId = scanRepository.insertScan(newBTScan)
            currentScanId = scanId
            bluetoothScanner.startScanBle()
        } catch (e: AppException.BluetoothUnavailableException) {
            Log.e(TAG, "Error starting bluetooth", e)
            currentScanId?.let{scanRepository.deleteScansById(listOf(it))}
            currentScanId = null
            throw e
        }
    }

    private fun observeDevices() {
        serviceScope.launch(Dispatchers.IO) {
            bluetoothScanner.devices.collectLatest { devices ->
                val scanId = currentScanId ?: return@collectLatest
                val location = locationTracker.location.value?: return@collectLatest
                val entries = toScanEntries(devices.values.toList(), scanId, location)

                if (entries.isNotEmpty()) {
                    scanEntryRepository.addEntries(entries)
                }
            }
        }
    }

    private fun toScanEntries(
        devices: List<BluetoothScanResult>,
        scanId: Long,
        location: Location
    ): List<BTScanEntry> {
        return devices.map {
            val manufacturerId = it.manufacturerData.keys.firstOrNull()

            BTScanEntry(
                scanId = scanId,
                timestamp = System.currentTimeMillis(),
                deviceAddress = it.deviceAddress,
                rssi = it.rssi,
                deviceName = it.deviceName,
                manufacturerId = manufacturerId,
                longitude = location.longitude,
                latitude = location.latitude,
                manufacturerName = scanEntryRepository.getCompanyName(manufacturerId)
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        bluetoothScanner.stopScanBle()
        locationTracker.stop()
        currentScanId = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleServiceError(e: Exception) {
        Log.e(TAG, "Scanning service error", e)

        showNotification(
            application,
            "Scanning error",
            text = e.message ?: "An error occurred during scanning",
            channelId = NotificationChannels.SCAN_SERVICE,
            id = NOTIFICATION_ID + 1
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onDestroy() {
        bluetoothScanner.stopScanBle()
        locationTracker.stop()
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}