package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import java.lang.System.currentTimeMillis

@Composable
fun PasswordRecoveryScreen(authViewModel: AuthViewModel = viewModel(), onBack: () -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    val mensajeRecuperacion by authViewModel.mensajeRecuperacion.observeAsState()
    val lastRequestTime by authViewModel.lastRequestTime.observeAsState(0L)
    val remainingTime by authViewModel.remainingTime.observeAsState(0L)

    LaunchedEffect(Unit) {
        authViewModel.calcularTiempoRestante()
        authViewModel.limpiarMensajeRecuperacion() // 🔹 Limpia el mensaje al entrar
        println("DEBUG: Pantalla cargada - lastRequestTime = $lastRequestTime, remainingTime = $remainingTime")
    }

    LaunchedEffect(remainingTime) {
        while (remainingTime > 0) {
            delay(1000L)
            authViewModel.reducirTiempoCooldown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Recuperación de Contraseña", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (mensajeRecuperacion?.isNotEmpty() == true) {
            Text(
                text = mensajeRecuperacion!!,
                color = if (mensajeRecuperacion == "Correo de recuperación enviado") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (correo.isNotBlank()) { // 🔹 Corregido: Solo envía si el correo no está vacío
                    authViewModel.recuperarContrasena(correo)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = remainingTime == 0L // Deshabilita el botón si hay cooldown activo
        ) {
            Text(text = if (remainingTime > 0) "Espera $remainingTime s" else "Enviar Correo de Recuperación")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text(text = "Volver a Iniciar Sesión")
        }
    }
}