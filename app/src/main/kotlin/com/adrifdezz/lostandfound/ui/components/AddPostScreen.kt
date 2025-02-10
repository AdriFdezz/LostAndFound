package com.adrifdezz.lostandfound.ui.components

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(authViewModel: AuthViewModel, navController: NavController) {
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var ultimaUbicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaPerdida by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMensaje by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fotoUri = uri
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            fechaPerdida = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Añadir Mascota Perdida",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("welcome_screen") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear Publicación",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre")
                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(value = edad, onValueChange = { edad = it }, label = "Edad")
                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(value = raza, onValueChange = { raza = it }, label = "Raza")
                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(value = localidad, onValueChange = { localidad = it }, label = "Localidad donde se perdió")
                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(value = ultimaUbicacion, onValueChange = { ultimaUbicacion = it }, label = "Última ubicación vista")
                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(value = descripcion, onValueChange = { descripcion = it }, label = "Descripción o datos de interés (Opcional)")
                Spacer(modifier = Modifier.height(16.dp))

                // Botón de selección de fecha
                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = fechaPerdida.ifEmpty { "Seleccionar Fecha" }, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Seleccionar Foto", color = Color.White, fontWeight = FontWeight.Bold)
                }

                fotoUri?.let { uri ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Foto seleccionada",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Eliminar Foto",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { fotoUri = null },
                            tint = Color(0xFFE57373)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMensaje.isNotEmpty()) {
                    Text(
                        text = errorMensaje,
                        color = Color(0xFFFF6F61),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val currentDate = Calendar.getInstance()
                        val selectedDate = fechaPerdida.split("/").let {
                            Calendar.getInstance().apply {
                                set(it[2].toInt(), it[1].toInt() - 1, it[0].toInt())
                            }
                        }
                        if (nombre.isBlank() || edad.isBlank() || raza.isBlank() ||
                            localidad.isBlank() || ultimaUbicacion.isBlank() || fechaPerdida.isBlank() || fotoUri == null
                        ) {
                            errorMensaje = "Completa los campos obligatorios"
                            return@Button
                        } else if (selectedDate.after(currentDate)) {
                            errorMensaje = "La fecha de pérdida no puede ser futura"
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
                            fechaPerdida,
                            fotoUri!!
                        ) { exitoso, error ->
                            if (exitoso) {
                                mostrarDialogo = true
                                nombre = ""
                                edad = ""
                                raza = ""
                                localidad = ""
                                ultimaUbicacion = ""
                                descripcion = ""
                                fechaPerdida = ""
                                fotoUri = null
                            } else {
                                errorMensaje = error ?: "Error desconocido al crear el post"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Publicar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
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