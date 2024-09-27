package com.example.dyf.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscuchaPorMiScreen() {
    val context = LocalContext.current
    var transcriptionText by remember { mutableStateOf("Presiona el botón para escuchar.") }
    var isListening by remember { mutableStateOf(false) }
    var buttonLabel by remember { mutableStateOf("Escuchar") }  // Texto del botón
    var speechRecognizer: SpeechRecognizer? = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    LaunchedEffect(Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            transcriptionText = "El reconocimiento de voz no está disponible."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.stopListening()  // Asegurarse de detener la escucha al destruir la vista
            speechRecognizer?.destroy()
        }
    }

    // Función para iniciar el reconocimiento de voz
    fun startListening() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), 1)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)  // 5 segundos de silencio para finalizar
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000)  // 1 segundo de mínimo de entrada
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                transcriptionText = "Escuchando..."
                isListening = true
                buttonLabel = "Detener"
            }

            override fun onBeginningOfSpeech() {
                transcriptionText = "Comienza a hablar..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                // No es necesario implementar si no se quiere medir el volumen de la voz
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // No se necesita implementar
            }

            override fun onEndOfSpeech() {
                transcriptionText = "Procesando..."
                isListening = false
                buttonLabel = "Escuchar"
            }

            override fun onError(error: Int) {
                transcriptionText = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio."
                    SpeechRecognizer.ERROR_CLIENT -> "Error del cliente."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes."
                    SpeechRecognizer.ERROR_NETWORK -> "Error de red."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera de red."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se encontró coincidencia."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor está ocupado. Intenta de nuevo."
                    SpeechRecognizer.ERROR_SERVER -> "Error del servidor."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó habla."
                    else -> "Error al procesar la voz. Código: $error"
                }
                Log.d("EscuchaPorMi", "Error al procesar: $error")
                isListening = false
                buttonLabel = "Escuchar"
            }

            override fun onResults(results: Bundle?) {
                val resultText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                    ?: "No se reconoció nada."
                transcriptionText = resultText
                Log.d("EscuchaPorMi", "Resultado: $resultText")
                isListening = false
                buttonLabel = "Escuchar"
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Se puede implementar si se quieren mostrar resultados parciales
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // No es necesario implementar
            }
        })

        speechRecognizer?.startListening(intent)
    }

    // Función para detener el reconocimiento de voz
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        buttonLabel = "Escuchar"
        transcriptionText = "Escucha detenida."
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        color = Color(0xFFF0F0F0)  // Color de fondo de la superficie
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Descripción de la pantalla
            Text(
                text = "Transcribe lo que escuchas a través del micrófono",
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar el texto transcrito o los mensajes
            Text(
                text = transcriptionText,
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para iniciar o detener el reconocimiento de voz
            Button(
                onClick = {
                    if (isListening) {
                        stopListening()
                    } else {
                        startListening()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = buttonLabel,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonLabel, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
