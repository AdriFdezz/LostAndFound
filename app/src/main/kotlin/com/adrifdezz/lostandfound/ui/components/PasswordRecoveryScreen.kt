package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PasswordRecoveryScreen(authViewModel: AuthViewModel = viewModel(), onBack: () -> Unit) {
    var correo by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf(false) }

    val mensajeRecuperacion by authViewModel.mensajeRecuperacion.observeAsState()
    val remainingTime by authViewModel.remainingTime.observeAsState(0L)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authViewModel.calcularTiempoRestante()
        authViewModel.limpiarMensajeRecuperacion()
    }

    var progress by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(remainingTime) {
        if (remainingTime > 0) {
            coroutineScope.launch {
                while (remainingTime > 0) {
                    delay(1000L)
                    progress = remainingTime / 60f
                }
            }
        } else {
            progress = 0f
        }
    }

    val errorColor = Color(0xFFFF6F61) // 🔹 Rojo menos brillante para errores
    val buttonBackground = Color.White.copy(alpha = 0.2f) // 🔹 Fondo gris semitransparente

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

                if (!mensajeRecuperacion.isNullOrEmpty()) {
                    Text(
                        text = mensajeRecuperacion ?: "",
                        color = if (mensajeRecuperacion == "Correo de recuperación enviado") Color.White else errorColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

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
                                errorCorreo = true
                            } else {
                                authViewModel.recuperarContrasena(correo)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .alpha(if (remainingTime > 0) 0.5f else 1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = if (remainingTime > 0) Color(0xFFB0BEC5) else Color(0xFF263238)
                        ),
                        enabled = remainingTime == 0L
                    ) {
                        Text(
                            text = if (remainingTime > 0) "Esperando..." else "Enviar correo de recuperación",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (remainingTime > 0) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            color = Color(0xFFB0BEC5),
                            trackColor = Color.Transparent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .background(buttonBackground, shape = CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
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
    }
}