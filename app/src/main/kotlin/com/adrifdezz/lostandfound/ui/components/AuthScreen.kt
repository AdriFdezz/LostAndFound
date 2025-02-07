package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.ui.utils.BotonMostrarOcultarContrasena
import com.adrifdezz.lostandfound.ui.utils.validarContrasena
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(authViewModel: AuthViewModel = viewModel(), navController: NavController) {
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

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (esModoRegistro) "Registrarse" else "Iniciar sesión",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (esModoRegistro) {
                AuthTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre")
            }

            Spacer(modifier = Modifier.height(8.dp))
            AuthTextField(value = correo, onValueChange = { correo = it }, label = "Correo electrónico")
            Spacer(modifier = Modifier.height(8.dp))
            AuthTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = "Contraseña",
                visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { BotonMostrarOcultarContrasena(mostrarContrasena) { mostrarContrasena = !mostrarContrasena } }
            )

            if (esModoRegistro) {
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it },
                    label = "Confirmar contraseña",
                    visualTransformation = if (mostrarConfirmarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { BotonMostrarOcultarContrasena(mostrarConfirmarContrasena) { mostrarConfirmarContrasena = !mostrarConfirmarContrasena } }
                )
            }

            if (mensajeError.isNotEmpty()) {
                Text(text = mensajeError, color = Color(0xFFFF6F61), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECEFF1), RoundedCornerShape(10.dp))
                    .height(50.dp),
                onClick = {
                    mensajeError = ""
                    if (esModoRegistro) {
                        if (nombre.isBlank()) mensajeError = "El nombre no puede estar vacío"
                        else if (correo.isBlank()) mensajeError = "El correo no puede estar vacío"
                        else if (!validarContrasena(contrasena)) mensajeError = "La contraseña debe tener al menos 8 caracteres y un número"
                        else if (contrasena != confirmarContrasena) mensajeError = "Las contraseñas no coinciden"
                        else authViewModel.registrar(correo, contrasena, nombre)
                    } else {
                        if (correo.isBlank()) mensajeError = "El correo no puede estar vacío"
                        else if (contrasena.isBlank()) mensajeError = "La contraseña no puede estar vacía"
                        else authViewModel.iniciarSesion(correo, contrasena)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = if (esModoRegistro) "Registrarse" else "Iniciar sesión",
                    color = Color(0xFF212121),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!esModoRegistro) {
                TextButton(onClick = { navController.navigate("password_recovery_screen") }) {
                    Text(text = "¿Olvidaste tu contraseña?", color = Color.White)
                }
            }

            TextButton(onClick = {
                esModoRegistro = !esModoRegistro
                mensajeError = ""
                correo = ""
                contrasena = ""
                confirmarContrasena = ""
                nombre = ""
            }) {
                Text(text = if (esModoRegistro) "¿Ya tienes una cuenta? Inicia sesión" else "¿No tienes una cuenta? Regístrate", color = Color.White)
            }
        }
    }
}

@Composable
fun AuthTextField(value: String, onValueChange: (String) -> Unit, label: String, visualTransformation: VisualTransformation = VisualTransformation.None, trailingIcon: @Composable (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
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
}