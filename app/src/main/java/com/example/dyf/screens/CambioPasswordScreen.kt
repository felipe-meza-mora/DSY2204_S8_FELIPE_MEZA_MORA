package com.example.dyf.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dyf.MenuActivity
import com.example.dyf.LoginActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambioPasswordScreen() {
    val context = LocalContext.current
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Cargar el correo del usuario desde SharedPreferences
    val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    val userEmail = sharedPreferences.getString("userEmail", "")
    val storedPassword = sharedPreferences.getString("userPassword", "")

    Log.d("CambioPassword", "Contraseña almacenada: $storedPassword")

    // Conectar a Firebase
    val database = FirebaseDatabase.getInstance().getReference("user")

    // Función de validación de contraseñas
    fun validatePasswords(): Boolean {
        Log.d("CambioPassword", "Validando contraseñas")
        return when {
            currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                errorMessage = "Todos los campos son obligatorios."
                Log.d("CambioPassword", "Error: Campos vacíos")
                false
            }
            currentPassword != storedPassword -> {
                errorMessage = "La contraseña actual es incorrecta."
                Log.d("CambioPassword", "Error: Contraseña actual incorrecta. Ingresada: $currentPassword, Almacenada: $storedPassword")
                false
            }
            newPassword != confirmPassword -> {
                errorMessage = "Las nuevas contraseñas no coinciden."
                Log.d("CambioPassword", "Error: Las contraseñas no coinciden")
                false
            }
            else -> {
                passwordError = null
                Log.d("CambioPassword", "Validación exitosa")
                true
            }
        }
    }

    // Función para vibración
    fun vibrate(context: Context, isSuccess: Boolean) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = if (isSuccess) {
                    VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)
                }
                vibrator.vibrate(vibrationEffect)
            } else {
                if (isSuccess) {
                    vibrator.vibrate(300)
                } else {
                    vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
                }
            }
        }
    }

    suspend fun updatePasswordInFirebase(newPassword: String) {
        try {
            // Buscar al usuario en Firebase usando el correo electrónico
            val snapshot = database.orderByChild("correo").equalTo(userEmail).get().await()

            if (snapshot.exists()) {
                for (userSnapshot in snapshot.children) {
                    // Actualizar la contraseña en la base de datos
                    userSnapshot.ref.child("password").setValue(newPassword)
                }
                successMessage = "Contraseña cambiada exitosamente."
                showSuccessDialog = true
                vibrate(context, true)
            } else {
                errorMessage = "No se encontró al usuario."
                showErrorDialog = true
                vibrate(context, false)
            }
        } catch (e: Exception) {
            errorMessage = "Error al actualizar la contraseña."
            showErrorDialog = true
            vibrate(context, false)
        }
    }

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
            imageVector = Icons.Default.Lock,
            contentDescription = "Candado",
            tint = Color.Black,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Cambiar Contraseña", fontSize = 24.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Contraseña Actual") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default,
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Contraseña Actual")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Nueva Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default,
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Nueva Contraseña")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Nueva Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default,
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirmar Nueva Contraseña")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (passwordError != null) {
            Text(passwordError ?: "", color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (validatePasswords()) {
                    // Actualizar la contraseña en Firebase y en SharedPreferences
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            updatePasswordInFirebase(newPassword)
                        }
                    }
                } else {
                    vibrate(context, false)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
        ) {
            Text("Cambiar Contraseña")
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Éxito") },
            text = { Text(successMessage, color = Color(0xFF000000), fontSize = 18.sp) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    onClick = {
                        showSuccessDialog = false

                        // Redirigir al LoginActivity
                        val intent = Intent(context, MenuActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Aceptar", fontSize = 18.sp, color = Color(0xFF000000))
                }
            }
        )
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage, color = Color(0xFF000000), fontSize = 18.sp) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5449)),
                    onClick = { showErrorDialog = false }
                ) {
                    Text("Aceptar", fontSize = 18.sp, color = Color(0xFFFFFFFF))
                }
            }
        )
    }
}
