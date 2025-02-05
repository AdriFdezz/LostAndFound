package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(authViewModel: AuthViewModel = viewModel()) {
    var esModoRegistro by remember { mutableStateOf(false) }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    // Observamos los estados de error y usuario registrado
    val errorState by authViewModel.error.observeAsState()
    val usuarioRegistrado by authViewModel.usuario.observeAsState()

    // Manejar errores
    LaunchedEffect(errorState) {
        errorState?.let {
            mensajeError = it
            correo = ""
            contrasena = ""
            confirmarContrasena = ""
            nombre = ""
        }
    }

    // Si el usuario se ha registrado, cambiar automáticamente a inicio de sesión
    LaunchedEffect(usuarioRegistrado) {
        usuarioRegistrado?.let {
            esModoRegistro = false  // Cambiar a pantalla de inicio de sesión automáticamente
            mensajeError = "" // Limpiar mensaje de error
        }
    }

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
            visualTransformation = PasswordVisualTransformation()
        )

        if (esModoRegistro) {
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = confirmarContrasena,
                onValueChange = { confirmarContrasena = it },
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
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
                    if (contrasena.isBlank()) {
                        mensajeError = "La contraseña no puede estar vacía"
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
}