package com.adrifdezz.lostandfound.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(authViewModel: AuthViewModel, navController: NavController) {
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var ultimaUbicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMensaje by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fotoUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Añadir Mascota Perdida") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("welcome_screen") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = nombre,
                onValueChange = { if (it.length <= 25) nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = edad,
                onValueChange = { if (it.length <= 15) edad = it },
                label = { Text("Edad") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = raza,
                onValueChange = { if (it.length <= 25) raza = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = localidad,
                onValueChange = { if (it.length <= 30) localidad = it },
                label = { Text("Localidad donde se perdió") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = ultimaUbicacion,
                onValueChange = { if (it.length <= 75) ultimaUbicacion = it },
                label = { Text("Última ubicación vista") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = descripcion,
                onValueChange = { if (it.length <= 175) descripcion = it },
                label = { Text("Descripción o datos de interés (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Seleccionar Foto")
            }
            fotoUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                val context = LocalContext.current
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.size(150.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (errorMensaje.isNotEmpty()) {
                Text(text = errorMensaje, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (nombre.isBlank() || edad.isBlank() || raza.isBlank() ||
                        localidad.isBlank() || ultimaUbicacion.isBlank() || fotoUri == null
                    ) {
                        errorMensaje = "Completa los cmapos obligatorios"
                        return@Button
                    }
                    errorMensaje = ""
                    authViewModel.publicarMascotaPerdida(
                        nombre,
                        edad,
                        raza,
                        localidad,
                        ultimaUbicacion,
                        descripcion,
                        fotoUri!!
                    ) { exitoso, error ->
                        if (exitoso) {
                            mostrarDialogo = true // Muestra el dialogo de éxito
                            // Limpia los campos del formulario
                            nombre = ""
                            edad = ""
                            raza = ""
                            localidad = ""
                            ultimaUbicacion = ""
                            descripcion = ""
                            fotoUri = null
                        } else {
                            errorMensaje = error ?: "Error desconocido al crear el post"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar")
            }
        }
    }

    if (mostrarDialogo) {
        CustomAlertDialogPost(
            title = "Post Creado!",
            message = "Tu publicación ha sido creada exitosamente.",
            onDismiss = { mostrarDialogo = false }
        )
    }
}

@Composable
fun CustomAlertDialogPost(
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