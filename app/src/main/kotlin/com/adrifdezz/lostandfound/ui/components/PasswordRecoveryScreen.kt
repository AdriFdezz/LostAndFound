package com.adrifdezz.lostandfound.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import com.adrifdezz.lostandfound.R
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PasswordRecoveryScreen(authViewModel: AuthViewModel = viewModel(), onBack: () -> Unit) {
    var correo by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val mensajeRecuperacion by authViewModel.mensajeRecuperacion.observeAsState()
    val remainingTime by authViewModel.remainingTime.observeAsState(0L)

    val coroutineScope = rememberCoroutineScope()
    val animatedProgress = remember { Animatable(1f) }

    val remainingTimeState = remember { mutableLongStateOf(remainingTime) }
    LaunchedEffect(remainingTime) {
        remainingTimeState.longValue = remainingTime
    }

    LaunchedEffect(Unit) {
        authViewModel.calcularTiempoRestante()
        authViewModel.limpiarMensajeRecuperacion()
        authViewModel.iniciarTemporizadorSiEsNecesario()
    }

    LaunchedEffect(mensajeRecuperacion) {
        if (mensajeRecuperacion == "Correo de recuperación enviado") {
            mostrarDialogo = true
        }
    }

    LaunchedEffect(key1 = remainingTime) {
        val cooldownDuration = authViewModel.cooldownTime / 1000f
        val currentProgress = remainingTime.toFloat() / cooldownDuration

        animatedProgress.snapTo(currentProgress)
    }

    val errorColor = Color(0xFFFF6F61)
    val buttonBackground = Color.White.copy(alpha = 0.2f)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_distorsion),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)))
        )

        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Recuperar Contraseña",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    modifier = Modifier.alpha(0.9f),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(buttonBackground, shape = RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    TextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                            errorCorreo = false
                            mensajeError = ""
                        },
                        label = { Text("Correo electrónico", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White
                        )
                    )
                }

                if (mensajeError.isNotEmpty()) {
                    Text(
                        text = mensajeError,
                        color = errorColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(if (remainingTime > 0) Color.Gray else Color(0xFFECEFF1), RoundedCornerShape(10.dp))
                ) {
                    Button(
                        onClick = {
                            if (correo.isBlank()) {
                                mensajeError = "Por favor, ingresa un correo válido."
                                errorCorreo = true
                            } else {
                                authViewModel.verificarCorreoEnFirestore(correo) { registrado, error ->
                                    if (registrado) {
                                        authViewModel.recuperarContrasena(correo)
                                        authViewModel.actualizarTiempoRestante(60)
                                    } else {
                                        mensajeError = error ?: "Error desconocido al verificar el correo."
                                        errorCorreo = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .alpha(if (remainingTimeState.longValue > 0) 0.5f else 1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = if (remainingTimeState.longValue > 0) Color(0xFFB0BEC5) else Color(0xFF263238)
                        ),
                        enabled = remainingTimeState.longValue == 0L
                    ) {
                        Text(
                            text = if (remainingTimeState.longValue > 0) "Esperando..." else "Enviar correo de recuperación",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (remainingTime > 0) {
                        LinearProgressIndicator(
                            progress = { animatedProgress.value },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .align(Alignment.BottomCenter),
                            color = Color.White.copy(alpha = 0.8f),
                            trackColor = Color.Transparent,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onBack) {
                    Text(
                        text = "Volver a Iniciar Sesión",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        CustomAlertDialogRecovery(
            title = "Revisa tu bandeja de entrada!",
            message = "Se ha enviado un correo de recuperación con éxito.",
            onDismiss = { mostrarDialogo = false }
        )
    }
}

@Composable
fun CustomAlertDialogRecovery(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.1f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF263238), Color(0xFF37474F))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_success), // Asegúrate de tener un icono
                contentDescription = null,
                tint = Color(0xFF4CAF50), // Verde éxito
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color(0xFFB0BEC5),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Aceptar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}