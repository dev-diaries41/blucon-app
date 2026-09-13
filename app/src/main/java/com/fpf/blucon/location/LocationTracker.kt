package com.fpf.blucon.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.fpf.blucon.errors.AppException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationTracker(context: Context) {

    private val context = context.applicationContext
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    val isLocationEnabled: Boolean
        get() = locationManager.isLocationEnabled

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _location.value = location
            _connected.value = true
        }

        override fun onProviderDisabled(provider: String) {
            _connected.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        checkPermission()

        val hasGpsProvider = isProviderAvailable(LocationManager.GPS_PROVIDER)
        val hasNetworkProvider = isProviderAvailable(LocationManager.NETWORK_PROVIDER)
        val hasFusedProvider = isProviderAvailable(LocationManager.FUSED_PROVIDER)

        if(listOf(hasGpsProvider, hasNetworkProvider, hasFusedProvider).all{ !it }) throw AppException.LocationUnavailableException("No valid location provider")
        if(!isLocationEnabled) throw AppException.LocationUnavailableException()

        if(hasFusedProvider) {
            locationManager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                1000L,
                1f,
                locationListener,
                Looper.getMainLooper()
            )
        }

        if(hasGpsProvider) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locationListener,
                Looper.getMainLooper()
            )
        }

        if(hasNetworkProvider) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                1f,
                locationListener,
                Looper.getMainLooper()
            )
        }

    }

    fun stop() {
        locationManager.removeUpdates(locationListener)
    }


    private fun checkPermission() {
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw AppException.LocationUnavailableException("Location permission not granted")
        }
    }

    private fun isProviderAvailable(provider: String): Boolean = locationManager.allProviders.contains(provider)
}