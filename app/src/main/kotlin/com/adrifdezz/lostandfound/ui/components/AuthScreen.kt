package com.adrifdezz.lostandfound.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adrifdezz.lostandfound.ui.utils.BotonMostrarOcultarContrasena
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import com.adrifdezz.lostandfound.ui.utils.validarContrasena
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(authViewModel: AuthViewModel = viewModel()) {
    var esModoRegistro by remember { mutableStateOf(false) }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var registroExitoso by remember { mutableStateOf(false) }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmarContrasena by remember { mutableStateOf(false) }

    val errorState by authViewModel.error.observeAsState()
    val usuarioRegistrado by authViewModel.usuario.observeAsState()

    LaunchedEffect(errorState) {
        errorState?.let {
            mensajeError = it
            correo = ""
            contrasena = ""
            confirmarContrasena = ""
            nombre = ""
        }
    }

    LaunchedEffect(usuarioRegistrado) {
        usuarioRegistrado?.let {
            esModoRegistro = false
            mensajeError = ""
            registroExitoso = true
        }
    }

    var progress by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            progress = 1f
            delay(5000)
            registroExitoso = false
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (registroExitoso) 0f else 1f,
        animationSpec = tween(durationMillis = 5000),
        label = "Animación de la barra de progreso"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (esModoRegistro) "Registrarse" else "Iniciar sesión",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (esModoRegistro) {
            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        TextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { BotonMostrarOcultarContrasena(mostrarContrasena) { mostrarContrasena = !mostrarContrasena } }
        )

        if (esModoRegistro) {
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = confirmarContrasena,
                onValueChange = { confirmarContrasena = it },
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (mostrarConfirmarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { BotonMostrarOcultarContrasena(mostrarConfirmarContrasena) { mostrarConfirmarContrasena = !mostrarConfirmarContrasena } }

            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (mensajeError.isNotEmpty()) {
            Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                mensajeError = ""

                if (esModoRegistro) {
                    if (nombre.isBlank()) {
                        mensajeError = "El nombre no puede estar vacío"
                        return@Button
                    }
                    if (correo.isBlank()) {
                        mensajeError = "El correo no puede estar vacío"
                        return@Button
                    }
                    if (!validarContrasena(contrasena)) {
                        mensajeError = "La contraseña debe tener al menos 8 caracteres y contener al menos un número"
                        return@Button
                    }
                    if (contrasena != confirmarContrasena) {
                        mensajeError = "Las contraseñas no coinciden"
                        return@Button
                    }

                    authViewModel.registrar(correo, contrasena, nombre)
                } else {
                    if (correo.isBlank()) {
                        mensajeError = "El correo no puede estar vacío"
                        return@Button
                    }
                    if (contrasena.isBlank()) {
                        mensajeError = "La contraseña no puede estar vacía"
                        return@Button
                    }

                    authViewModel.iniciarSesion(correo, contrasena)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (esModoRegistro) "Registrarse" else "Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                esModoRegistro = !esModoRegistro
                mensajeError = ""
                correo = ""
                contrasena = ""
                confirmarContrasena = ""
                nombre = ""
            }
        ) {
            Text(
                text = if (esModoRegistro) "¿Ya tienes una cuenta? Inicia sesión" else "¿No tienes una cuenta? Regístrate"
            )
        }
    }

    if (registroExitoso) {
        Snackbar(
            modifier = Modifier
                .padding(16.dp)
                .alpha(0.9f),
            shape = RoundedCornerShape(8.dp),
            containerColor = Color(0xFFD1C4E9),
            action = {
                IconButton(onClick = { registroExitoso = false }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.Black
                    )
                }
            }
        ) {
            Column {
                Text(
                    text = "Registro exitoso",
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color.Gray,
                )
            }
        }
    }