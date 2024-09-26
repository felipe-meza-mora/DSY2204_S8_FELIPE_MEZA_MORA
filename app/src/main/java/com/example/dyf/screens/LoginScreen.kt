package com.example.dyf.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.dyf.MenuActivity
import com.example.dyf.OlvidasteActivity
import com.example.dyf.R
import com.example.dyf.RegistrarseActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isFormValid by remember { mutableStateOf(false) }

    // Validación de formulario
    fun validateForm() {
        emailError = when {
            email.isBlank() -> "El correo no puede estar vacío"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "El correo no es válido"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "La contraseña no puede estar vacía"
            else -> null
        }
        isFormValid = emailError == null && passwordError == null
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
                    vibrator.vibrate(300) // Vibración única de 300ms
                } else {
                    vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1) // Vibración patrón
                }
            }
        }
    }

    // Validar credenciales con Firebase
    fun validateCredentials() {
        validateForm()
        if (isFormValid) {
            val database = FirebaseDatabase.getInstance().getReference("user")
            database.orderByChild("correo").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val dbPassword = userSnapshot.child("password").getValue(String::class.java)
                            if (dbPassword == password) {
                                // Autenticación exitosa
                                val userName = userSnapshot.child("nombreCompleto").getValue(String::class.java)

                                // Guardar el nombre y el correo del usuario en SharedPreferences
                                val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("userName", userName)
                                editor.putString("userEmail", email)
                                editor.apply()

                                vibrate(context, true)
                                val intent = Intent(context, MenuActivity::class.java)
                                context.startActivity(intent)

                                //(context as Activity).finish()
                            } else {
                                // Contraseña incorrecta
                                vibrate(context, false)
                                passwordError = "Contraseña incorrecta"
                            }
                        }
                    } else {
                        // El correo no está registrado
                        vibrate(context, false)
                        emailError = "Correo no registrado"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejo de errores con la base de datos
                    vibrate(context, false)
                    emailError = "Error de conexión con la base de datos"
                }
            })
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F0F0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.dyf),
                contentDescription = "Logo DyF",
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .padding(bottom = 32.dp)
            )

            // Campo de Correo Electrónico
            val emailColor by animateColorAsState(targetValue = if (emailError == null) Color(0xFFFFC107) else Color(0xFFFF5449))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    validateForm()
                },
                label = { Text("Correo Electrónico") },
                isError = emailError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo de entrada de Correo Electrónico" },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Icono de Correo Electrónico",
                        tint = emailColor
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = emailColor,
                    cursorColor = emailColor
                )
            )
            if (emailError != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Error de Correo",
                        tint = Color(0xFFFF5449)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = emailError ?: "",
                        color = Color(0xFFFF5449),
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Contraseña
            val passwordColor by animateColorAsState(targetValue = if (passwordError == null) Color(0xFFFFC107) else Color(0xFFFF5449))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    validateForm()
                },
                label = { Text("Contraseña") },
                isError = passwordError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo de entrada de Contraseña" },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Icono de Contraseña",
                        tint = passwordColor
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = passwordColor,
                    cursorColor = passwordColor
                )
            )
            if (passwordError != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Error de Contraseña",
                        tint = Color(0xFFFF5449)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = passwordError ?: "",
                        color = Color(0xFFFF5449),
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para Iniciar Sesión
            Button(
                onClick = { validateCredentials() },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color(0xFFFFC107) else Color(0xFFCCCCCC)
                )
            ) {
                Text("Iniciar Sesión", fontSize = 18.sp, color = Color(0xFF000000))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto para recuperación de contraseña y registro
            Text(
                "¿Olvidaste tu contraseña?",
                color = Color(0xFF969088),
                fontSize = 20.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    val intent = Intent(context, OlvidasteActivity::class.java)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Registrarse",
                color = Color(0xFF969088),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    val intent = Intent(context, RegistrarseActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }
    }
}
