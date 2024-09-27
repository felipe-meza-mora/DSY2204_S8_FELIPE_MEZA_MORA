package com.example.dyf.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.dyf.MenuActivity
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarDispositivoScreen(
    requestLocation: (Location) -> Unit
) {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Ubicación no obtenida") }
    var addressText by remember { mutableStateOf("Dirección no disponible") }

    // Crear una corrutina para manejar las funciones suspendidas
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Botón Volver
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = {
                    val intent = Intent(context, MenuActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver al Menu",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ícono de Candado
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Geo",
            tint = Color.Black,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text(text = locationText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = addressText, fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            // Ejecutar la lógica en una corrutina
            scope.launch {
                // Obtener la última ubicación
                getLastKnownLocation(context)?.let { location ->
                    // Actualizamos la UI con la latitud y longitud
                    locationText = "Latitud: ${location.latitude}, Longitud: ${location.longitude}"

                    // Obtener la dirección a partir de la latitud y longitud
                    val address = convertLocationToAddress(context, location)
                    addressText = address ?: "No se pudo obtener la dirección."
                }
            }
        },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)))
        {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Obtener Ubicación",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(text = "Obtener Ubicación")
        }
    }
}

// Función suspendida que convierte la ubicación en dirección
suspend fun convertLocationToAddress(context: Context, location: Location): String? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                "${address.getAddressLine(0)}, ${address.locality}"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

// Obtener la última ubicación
suspend fun getLastKnownLocation(context: Context): Location? {
    return withContext(Dispatchers.IO) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.await()
        } catch (e: SecurityException) {
            null
        }
    }
}

