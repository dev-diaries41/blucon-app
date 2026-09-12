package com.fpf.blucon.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationTracker(context: Context) {

    private val context = context.applicationContext
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var locationContinuation: CancellableContinuation<Location>? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationContinuation?.let { continuation ->
                locationContinuation = null
                locationManager.removeUpdates(this)

                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }
        }

        override fun onProviderDisabled(provider: String) {
            locationContinuation?.let { continuation ->
                locationContinuation = null
                locationManager.removeUpdates(this)

                if (continuation.isActive) {
                    continuation.resumeWithException(
                        IllegalStateException("Location provider disabled")
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun start(): Location {
        checkPermission()

        getLastKnownLocation()?.let {
            return it
        }

        if (!isLocationEnabled()) {
            throw IllegalStateException("Location provider is disabled")
        }

        return suspendCancellableCoroutine { continuation ->
            locationContinuation = continuation

            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    locationListener,
                    Looper.getMainLooper()
                )
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    1f,
                    locationListener,
                    Looper.getMainLooper()
                )

                continuation.invokeOnCancellation {
                    if (locationContinuation === continuation) {
                        locationContinuation = null
                    }
                    locationManager.removeUpdates(locationListener)
                }
            } catch (e: Exception) {
                if (locationContinuation === continuation) {
                    locationContinuation = null
                }

                locationManager.removeUpdates(locationListener)
                continuation.resumeWithException(e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun stop() {
        locationContinuation?.let { continuation ->
            locationContinuation = null

            if (continuation.isActive) {
                continuation.cancel()
            }
        }

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
            throw IllegalStateException("Location permission not granted")
        }
    }
}