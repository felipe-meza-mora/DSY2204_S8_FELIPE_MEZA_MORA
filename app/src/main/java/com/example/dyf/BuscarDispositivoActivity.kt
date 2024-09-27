package com.example.dyf

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.dyf.screens.BuscarDispositivoScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class BuscarDispositivoActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            requestLocation { location ->
                // Llamar a la función cuando la ubicación sea obtenida
                updateUIWithLocation(location)
            }
        } else {
            // Manejar el caso cuando el permiso no es otorgado
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            BuscarDispositivoScreen(
                requestLocation = { location ->
                    updateUIWithLocation(location)
                }
            )
        }
    }

    private fun requestLocation(onLocationUpdate: (Location) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {

                    onLocationUpdate(location)
                } else {
                    Log.d("Location", "No se pudo obtener la ubicación")
                }
            }
        } else {
            // Solicitar permisos
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun updateUIWithLocation(location: Location) {
        Log.d("Location", "Latitud: ${location.latitude}, Longitud: ${location.longitude}")

    }
}
