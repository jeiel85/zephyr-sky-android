package com.jeiel.zephyr_sky.ui.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class DeviceLocationSelection(
    val latitude: Double,
    val longitude: Double,
    val displayName: String
)

object DeviceLocationResolver {
    private const val LOCATION_TIMEOUT_MS = 10_000L

    suspend fun resolve(context: Context): DeviceLocationSelection = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            throw SecurityException("현재 위치를 사용하려면 위치 권한이 필요합니다.")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = getBestKnownLocation(locationManager)
            ?: requestSingleLocation(locationManager)
            ?: throw IllegalStateException("현재 위치를 확인할 수 없습니다. 기기의 위치 서비스를 켠 뒤 다시 시도해 주세요.")

        DeviceLocationSelection(
            latitude = location.latitude,
            longitude = location.longitude,
            displayName = resolveDisplayName(context, location)
        )
    }

    fun hasLocationPermission(context: Context): Boolean {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun getBestKnownLocation(locationManager: LocationManager): Location? {
        return enabledProviders(locationManager)
            .mapNotNull { provider ->
                try {
                    locationManager.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
            .maxByOrNull { it.time }
    }

    private suspend fun requestSingleLocation(locationManager: LocationManager): Location? {
        val provider = enabledProviders(locationManager).firstOrNull() ?: return null
        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location)
                    }

                    @Deprecated("Deprecated in platform API")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                    override fun onProviderEnabled(provider: String) = Unit
                    override fun onProviderDisabled(provider: String) = Unit
                }

                try {
                    @Suppress("DEPRECATION")
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                    continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
                } catch (_: SecurityException) {
                    if (continuation.isActive) continuation.resume(null)
                } catch (_: IllegalArgumentException) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
    }

    private fun enabledProviders(locationManager: LocationManager): List<String> {
        val preferred = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        return preferred.filter { provider ->
            try {
                locationManager.isProviderEnabled(provider)
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun resolveDisplayName(context: Context, location: Location): String {
        val fallback = "현재 위치"
        return try {
            val geocoder = Geocoder(context, Locale.KOREAN)
            @Suppress("DEPRECATION")
            val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()

            address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea
                ?: address?.countryName
                ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
