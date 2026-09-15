package com.fpf.blucon.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fpf.blucon.errors.AppException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationTracker(context: Context) {
    private val context = context.applicationContext
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _isLocationEnabled = MutableStateFlow(locationManager.isLocationEnabled)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val locationStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != LocationManager.PROVIDERS_CHANGED_ACTION) return
            _isLocationEnabled.value = locationManager.isLocationEnabled
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _location.value = location
        }

        override fun onProviderDisabled(provider: String) {}
    }

    init {
        ContextCompat.registerReceiver(
            context,
            locationStateReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    @SuppressLint("MissingPermission")
    fun start() {
        checkPermission()

        if (!isLocationEnabled.value) {
            throw AppException.LocationUnavailableException()
        }

        val providers = listOf(
            LocationManager.FUSED_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER
        ).filter(::isProviderAvailable)

        val now = System.currentTimeMillis()

        providers
            .mapNotNull { locationManager.getLastKnownLocation(it) }
            .filter { now - it.time <= 60_000L }
            .maxByOrNull { it.time }
            ?.let { _location.value = it }

        val activeProvider = when {
            isProviderAvailable(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER
            isProviderAvailable(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            isProviderAvailable(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> throw AppException.LocationUnavailableException("No valid location provider")
        }

        locationManager.requestLocationUpdates(
            activeProvider,
            1000L,
            1f,
            locationListener,
            Looper.getMainLooper()
        )

        _isTracking.value = true
//        Log.d("locationtracker", "is tracking set")
    }

    fun stop() {
        locationManager.removeUpdates(locationListener)
        _isTracking.value = false
    }

    private fun checkPermission() {
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            )
        {
            throw AppException.LocationUnavailableException("Location permission not granted")
        }
    }

    private fun isProviderAvailable(provider: String): Boolean =
        locationManager.allProviders.contains(provider)
}