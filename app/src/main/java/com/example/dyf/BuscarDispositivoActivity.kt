package com.example.dyf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.dyf.screens.BuscarDispositivoScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BuscarDispositivoActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Lanzador para solicitar permiso de ubicación
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Si se otorga el permiso, solicita la ubicación
            requestLocation { location ->
                updateUIWithLocation(location)
            }
        } else {
            // Si se deniega el permiso, mostrar diálogo explicando por qué se necesita
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            BuscarDispositivoScreen(
                requestLocation = {
                    requestLocation { location ->
                        updateUIWithLocation(location)
                    }
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
            // Obtener la última ubicación conocida
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationUpdate(location)
                } else {
                    Log.d("Location", "No se pudo obtener la ubicación")
                }
            }
        } else {
            // Solicitar permisos si no están otorgados
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permiso de GPS denegado")
            .setMessage("Esta aplicación necesita acceso al GPS para obtener la ubicación del dispositivo. Por favor, habilite el permiso en la configuración.")
            .setPositiveButton("Abrir configuración") { _, _ ->
                // Abre la configuración de la app para que el usuario habilite los permisos manualmente
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = android.net.Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateUIWithLocation(location: Location) {
        Log.d("Location", "Latitud: ${location.latitude}, Longitud: ${location.longitude}")
        // Aquí puedes agregar el código para actualizar la UI con la ubicación obtenida
    }
}
