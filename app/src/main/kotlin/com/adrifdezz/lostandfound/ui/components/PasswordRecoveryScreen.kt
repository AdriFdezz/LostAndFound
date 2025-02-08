package com.adrifdezz.lostandfound.ui.components

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PasswordRecoveryScreen(authViewModel: AuthViewModel = viewModel(), onBack: () -> Unit) {
    var correo by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }  // 🔹 Estado para mostrar el diálogo

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
        Log.d("DEBUG_DIALOGO", "mensajeRecuperacion: $mensajeRecuperacion") // Log para depurar
        if (mensajeRecuperacion == "Correo de recuperación enviado") {
            mostrarDialogo = true
            Log.d("DEBUG_DIALOGO", "Diálogo activado") // Log cuando se activa el diálogo
        }
    }

    LaunchedEffect(mostrarDialogo) {
        if (!mostrarDialogo) {
            authViewModel.limpiarMensajeRecuperacion() // Limpia el mensaje después de cerrar el diálogo
        }
    }

    LaunchedEffect(remainingTime) {
        Log.d("DEBUG_REMAINING_TIME", "Composable observando remainingTime: $remainingTime")
        if (remainingTime > 0) {
            animatedProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = (remainingTime * 1000).toInt(), easing = LinearEasing)
            )
        } else {
            coroutineScope.launch {
                animatedProgress.snapTo(1f)
                Log.d("DEBUG_REMAINING_TIME", "Animación reiniciada, remainingTime llegó a 0")
            }
        }
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

                if (errorCorreo) {
                    Text(
                        text = "El correo no puede estar vacío.",
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
                            Log.d("DEBUG_REMAINING_TIME", "Botón presionado, correo: $correo")
                            if (correo.isBlank()) {
                                errorCorreo = true
                                Log.d("DEBUG_REMAINING_TIME", "Error: El correo está vacío")
                            } else {
                                authViewModel.recuperarContrasena(correo)
                                authViewModel.actualizarTiempoRestante(60)
                                Log.d("DEBUG_REMAINING_TIME", "Correo enviado y temporizador reiniciado")
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
                            trackColor = Color.Transparent
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
        Log.d("DEBUG_DIALOGO", "Mostrando el diálogo correctamente")
        CustomAlertDialog(
            title = "Revisa tu bandeja de entrada!",
            message = "Se ha enviado un correo de recuperación con éxito.",
            onDismiss = { mostrarDialogo = false }
        )
    }
}

@Composable
fun CustomAlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.1f)) // Fondo semi-transparente
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
            // Icono
            Icon(
                painter = painterResource(id = R.drawable.ic_success), // Asegúrate de tener un icono
                contentDescription = null,
                tint = Color(0xFF4CAF50), // Verde éxito
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Título
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Mensaje
            Text(
                text = message,
                color = Color(0xFFB0BEC5),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Botón de confirmación
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