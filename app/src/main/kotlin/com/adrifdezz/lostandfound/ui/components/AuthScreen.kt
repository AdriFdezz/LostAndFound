package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.ui.utils.BotonMostrarOcultarContrasena
import com.adrifdezz.lostandfound.ui.utils.validarContrasena
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel

/**
 * Pantalla de autenticación que permite al usuario registrarse o iniciar sesión.
 *
 * @param authViewModel ViewModel que maneja la lógica de autenticación.
 * @param navController Controlador de navegación para moverse entre pantallas.
 */
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
    var aceptarTerminos by remember { mutableStateOf(false) }
    var mostrarErrorTerminos by remember { mutableStateOf(false) }
    var mostrarDetallesTerminos by remember { mutableStateOf(false) }

    val errorState by authViewModel.error.observeAsState()
    val usuarioRegistrado by authViewModel.usuario.observeAsState()
    val mostrarAlerta = authViewModel.mostrarAlertaCorreoVerificado.observeAsState(initial = false)
    val mostrarAlertaCuentaEliminada = authViewModel.mostrarAlertaCuentaEliminada.observeAsState(false)

    // Manejador de errores
    LaunchedEffect(errorState) {
        errorState?.let {
            mensajeError = it
            correo = ""
            contrasena = ""
            confirmarContrasena = ""
            nombre = ""
        }
    }

    // Observa si el usuario se ha registrado con éxito
    LaunchedEffect(usuarioRegistrado) {
        usuarioRegistrado?.let {
            registroExitoso = esModoRegistro
            esModoRegistro = false
            mensajeError = ""
        }
    }

    if (mostrarAlerta.value) {
        CustomAlertDialogCorreo(
            title = "Verificación de correo",
            message = "Se ha enviado un correo de verificación a tu nuevo correo. Confírmalo antes de iniciar sesión nuevamente.",
            onDismiss = { authViewModel.desactivarAlertaCorreoVerificado() }
        )
    }
    if (mostrarAlertaCuentaEliminada.value) {
        CustomAlertDialogCuentaEliminada(
            title = "Cuenta eliminada",
            message = "Tu cuenta ha sido eliminada exitosamente. Gracias por usar nuestra aplicación.",
            onDismiss = { authViewModel.desactivarAlertaCuentaEliminada() }
        )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
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

            // Campo de contraseña con botón de mostrar/ocultar
            AuthTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = "Contraseña",
                visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { BotonMostrarOcultarContrasena(mostrarContrasena) { mostrarContrasena = !mostrarContrasena } }
            )

            if (esModoRegistro) {
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de confirmación de contraseña
                AuthTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it },
                    label = "Confirmar contraseña",
                    visualTransformation = if (mostrarConfirmarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { BotonMostrarOcultarContrasena(mostrarConfirmarContrasena) { mostrarConfirmarContrasena = !mostrarConfirmarContrasena } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Checkbox terminos y condiciones + recogida de datos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = aceptarTerminos,
                        onCheckedChange = {
                            aceptarTerminos = it
                            mostrarErrorTerminos = false
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.DarkGray,
                            checkmarkColor = Color.DarkGray
                        )
                    )
                    Text(
                        text = "Acepto los términos y condiciones y la recolección de mis datos.",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { mostrarDetallesTerminos = !mostrarDetallesTerminos },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }

                if (mostrarDetallesTerminos) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Detalles de los términos y condiciones:",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Al registrarte, aceptas que tus datos sean almacenados de forma segura.\n" +
                                    "• Solo se utilizarán para gestionar la aplicación y no serán compartidos con terceros.\n" +
                                    "• Puedes solicitar la eliminación de tu cuenta y todos tus datos en cualquier momento.\n" +
                                    "• Nos reservamos el derecho de modificar estos términos en futuras versiones de la app.",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (mostrarErrorTerminos) {
                    Text(
                        text = "Debes aceptar los términos y condiciones para registrarte.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (mensajeError.isNotEmpty()) {
                Text(text = mensajeError, color = Color(0xFFFF6F61), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de registro o inicio de sesión
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
                        else if (!aceptarTerminos) {
                            mostrarErrorTerminos = true
                        } else {
                            authViewModel.registrar(correo, contrasena, nombre)
                        }
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

            // Botón para recuperación de contraseña
            if (!esModoRegistro) {
                TextButton(onClick = { navController.navigate("password_recovery_screen") }) {
                    Text(text = "¿Olvidaste tu contraseña?", color = Color.White)
                }
            }

            // Botón para cambiar entre iniciar sesión y registrarse
            TextButton(onClick = {
                esModoRegistro = !esModoRegistro
                mensajeError = ""
                correo = ""
                contrasena = ""
                confirmarContrasena = ""
                nombre = ""
                aceptarTerminos = false
                mostrarErrorTerminos = false
            }) {
                Text(text = if (esModoRegistro) "¿Ya tienes una cuenta? Inicia sesión" else "¿No tienes una cuenta? Regístrate", color = Color.White)
            }
        }
    }

    // Diálogo de éxito tras un registro exitoso
    if (registroExitoso) {
        CustomAlertDialogAuth(
            title = "Registro exitoso",
            message = "Tu cuenta ha sido creada con éxito. Ahora puedes iniciar sesión.",
            onDismiss = { registroExitoso = false }
        )
    }
}

/**
 * Campo de texto reutilizable para la autenticación.
 *
 * @param value Valor actual del campo.
 * @param onValueChange Callback cuando el valor cambia.
 * @param label Etiqueta del campo de texto.
 * @param visualTransformation Transformación visual del texto (ej. ocultar contraseñas).
 * @param trailingIcon Ícono adicional en el campo de texto (ej. botón de mostrar contraseña).
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
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
            keyboardOptions = KeyboardOptions.Default,
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

/**
 * Diálogo de alerta personalizado para confirmar un registro exitoso.
 *
 * Este diálogo muestra un mensaje de éxito cuando el usuario ha completado correctamente
 * el registro en la aplicación y le permite cerrar el mensaje.
 *
 * @param title Título del diálogo.
 * @param message Mensaje de confirmación que se mostrará en el diálogo.
 * @param onDismiss Acción que se ejecuta cuando el usuario presiona el botón "Aceptar".
 */
@Composable
fun CustomAlertDialogAuth(
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
                painter = painterResource(id = R.drawable.ic_success),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
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

/**
 * Diálogo de alerta personalizado para notificar sobre el envío de un correo.
 *
 * Este diálogo muestra un mensaje informando al usuario que se ha enviado un correo,
 * permitiéndole cerrar el mensaje al presionar el botón "Aceptar".
 *
 * @param title Título del diálogo.
 * @param message Mensaje informativo que se mostrará en el diálogo.
 * @param onDismiss Acción que se ejecuta cuando el usuario presiona el botón "Aceptar".
 */
@Composable
fun CustomAlertDialogCorreo(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                painter = painterResource(id = R.drawable.ic_mail), // Ícono de correo
                contentDescription = null,
                tint = Color(0xFF4CAF50), // Mantiene el mismo color verde que el de Auth
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
                    containerColor = Color(0xFF4CAF50), // Botón verde igual que en `Auth`
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

/**
 * Diálogo de alerta personalizado para confirmar la eliminación de una cuenta.
 *
 * Este diálogo muestra un mensaje de advertencia informando al usuario que su cuenta ha sido eliminada,
 * permitiéndole cerrar el mensaje al presionar el botón "Aceptar".
 *
 * @param title Título del diálogo.
 * @param message Mensaje de advertencia que se mostrará en el diálogo.
 * @param onDismiss Acción que se ejecuta cuando el usuario presiona el botón "Aceptar".
 */
@Composable
fun CustomAlertDialogCuentaEliminada(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = null,
                tint = Color(0xFFD32F2F),
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
                    containerColor = Color(0xFFD32F2F),
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