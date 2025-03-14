package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Pantalla de perfil del usuario.
 *
 * Esta pantalla permite al usuario visualizar y actualizar su información personal,
 * incluyendo su nombre y correo electrónico, así como eliminar su cuenta.
 *
 * Funcionalidades:
 * - Muestra el nombre y correo actual del usuario.
 * - Permite actualizar el nombre del usuario.
 * - Permite actualizar el correo electrónico del usuario, requiriendo su contraseña actual.
 * - Permite eliminar la cuenta del usuario tras autenticarse nuevamente.
 *
 * @param authViewModel ViewModel de autenticación para gestionar la información del usuario.
 * @param navController Controlador de navegación para gestionar las transiciones entre pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(authViewModel: AuthViewModel, navController: NavController) {
    LocalContext.current
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoCorreo by remember { mutableStateOf("") }
    var contrasenaActual by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var mensajeExito by remember { mutableStateOf("") }
    val usuario = authViewModel.usuario.value
    var nombreActual by remember { mutableStateOf("Cargando...") }
    var correoActual by remember { mutableStateOf("Cargando...") }
    rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authViewModel.refrescarUsuarioYActualizarCorreo()
    }

    LaunchedEffect(usuario) {
        usuario?.let {
            correoActual = it.email ?: "Desconocido"
            FirebaseFirestore.getInstance().collection("usuarios").document(it.uid)
                .get().addOnSuccessListener { document ->
                    nombreActual = document.getString("nombre") ?: "Desconocido"
                }.addOnFailureListener {
                    nombreActual = "Error al cargar"
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Perfil", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Actualizar Información",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Nombre actual: $nombreActual", color = Color.White)
                Text("Correo actual: $correoActual", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = "Nuevo Nombre")
                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(value = nuevoCorreo, onValueChange = { nuevoCorreo = it }, label = "Nuevo Correo Electrónico")
                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(
                    value = contrasenaActual,
                    onValueChange = { contrasenaActual = it },
                    label = "Contraseña Actual",
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        mensajeError = ""
                        mensajeExito = ""

                        if (nuevoNombre.isBlank()) {
                            mensajeError = "El nombre no puede estar vacío."
                        } else {
                            authViewModel.actualizarNombre(nuevoNombre) { exito, mensaje ->
                                if (exito) {
                                    mensajeExito = mensaje
                                    nombreActual = nuevoNombre
                                } else {
                                    mensajeError = mensaje
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Actualizar Nombre", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        mensajeError = ""
                        mensajeExito = ""

                        if (nuevoCorreo.isBlank() || contrasenaActual.isBlank()) {
                            mensajeError = "Debes ingresar tu contraseña actual y el nuevo correo."
                        } else {
                            authViewModel.actualizarCorreo(nuevoCorreo, contrasenaActual, navController) { exito, mensaje ->
                                if (exito) {
                                    authViewModel.activarAlertaCorreoVerificado()
                                    navController.navigate("auth_screen") {
                                        popUpTo("auth_screen") { inclusive = true }
                                    }
                                } else {
                                    mensajeError = mensaje
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Actualizar Correo", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        mensajeError = ""

                        if (contrasenaActual.isBlank()) {
                            mensajeError = "Debes ingresar tu contraseña para eliminar la cuenta."
                        } else {
                            authViewModel.eliminarCuenta(contrasenaActual, navController) { exito, mensaje ->
                                if (!exito) {
                                    mensajeError = mensaje
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar Cuenta", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (mensajeError.isNotEmpty()) {
                    Text(text = mensajeError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}