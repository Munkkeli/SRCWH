package com.example.srcwh

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

enum class LocationError {
    DENIED,
    BLOCKED,
    EXPLAIN,
    GENERIC
}

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

class LocationHandler {
    private val context: Context
    private val fusedLocationClient: FusedLocationProviderClient

    var locationRequestCallback: ((granted: Boolean) -> Unit)? = null

    constructor(context: Context) {
        this.context = context
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun getCoordinates(comingFromExplain: Boolean = false, callback: (error: LocationError?, coordinates: Coordinates?) -> Unit) {
        Log.d("LOCATION", "getLocationCoordinates")

        checkLocationPermission(comingFromExplain) { error, granted ->
            Log.d("LOCATION", "checkLocationPermission $error $granted")

            if (error != null) {
                callback(error, null)
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        Log.d("LOCATION", "Last location returned ${location?.longitude}, ${location?.latitude}")

                        if (location == null) {
                            callback(LocationError.GENERIC, null)
                        } else {
                            callback(null, Coordinates(location.longitude, location.latitude))
                        }
                    }
                    .addOnFailureListener {
                        Log.d("LOCATION", "Last location failed")
                        callback(LocationError.GENERIC, null)
                    }
            }
        }
    }

    private fun checkLocationPermission(isAlreadyExplained: Boolean, callback: (error: LocationError?, granted: Boolean) -> Unit) {
        val isPermissionGranted = checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
        val isExplanationRequired = shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_FINE_LOCATION)

        fun askForPermission() {
            requestLocationPermission { granted ->
                when {
                    // We have permission, all clear
                    granted -> callback(null, true)

                    // Consider location as blocked
                    else -> callback(LocationError.BLOCKED, false)
                }
            }
        }

        when {
            // We have permission, all clear
            isPermissionGranted -> callback(null, true)

            // We do need to explain, and have not yet done so
            isExplanationRequired && !isAlreadyExplained -> callback(LocationError.EXPLAIN, false)

            // We have explained, ask for permission again
            isExplanationRequired && isAlreadyExplained -> askForPermission()

            // We do not need to explain, just ask for location
            !isExplanationRequired -> askForPermission()

            // Something unexpected happened
            else -> callback(LocationError.GENERIC, false)
        }
    }

    private fun requestLocationPermission(callback: (granted: Boolean) -> Unit) {
        Log.d("LOCATION", "requestLocationPermission")

        locationRequestCallback = callback
        requestPermissions(context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("LOCATION", "onRequestPermissionsResult ${requestCode}")

        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                val granted = (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                if (locationRequestCallback != null) {
                    locationRequestCallback?.invoke(granted)
                    locationRequestCallback = null
                }
            }
            else -> {}
        }
    }
}