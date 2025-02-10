package com.adrifdezz.lostandfound.ui.components

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.data.PostData
import com.google.firebase.firestore.FirebaseFirestore
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(postId: String, navController: NavController) {
    val context = LocalContext.current
    var post by remember { mutableStateOf<PostData?>(null) }
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var ultimaUbicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaPerdida by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var fotoUrl by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fotoUri = uri
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year: Int, month: Int, dayOfMonth: Int ->
            fechaPerdida = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(postId) {
        try {
            val document = FirebaseFirestore.getInstance()
                .collection("mascotas_perdidas")
                .document(postId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.toObject(PostData::class.java)
                data?.let {
                    post = it
                    nombre = it.nombre
                    edad = it.edad
                    raza = it.raza
                    localidad = it.localidad
                    ultimaUbicacion = it.ultimaUbicacion
                    descripcion = it.descripcion
                    fechaPerdida = it.diaPerdido
                    fotoUrl = it.fotoUrl
                }
            }
        } catch (e: Exception) {
            errorMensaje = "Error al cargar los datos"
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Editar Publicación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver"
                        )
                    }
                }
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

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Cambiar datos de publicación",
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

                    Box(modifier = Modifier.size(150.dp)) {
                        fotoUri?.let { uri ->
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Nueva Imagen",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } ?: run {
                            GlideImage(
                                imageModel = fotoUrl,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentDescription = "Imagen Actual"
                            )
                        }
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
                        Text(text = "Cambiar Foto", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (nombre.isBlank() || edad.isBlank() || raza.isBlank() || localidad.isBlank() ||
                                ultimaUbicacion.isBlank() || fechaPerdida.isBlank()
                            ) {
                                errorMensaje = "Todos los campos son obligatorios"
                                return@Button
                            }

                            val updateData = mapOf(
                                "nombre" to nombre,
                                "edad" to edad,
                                "raza" to raza,
                                "localidad" to localidad,
                                "ultimaUbicacion" to ultimaUbicacion,
                                "descripcion" to descripcion,
                                "diaPerdido" to fechaPerdida,
                                "fotoUrl" to (fotoUri?.toString() ?: fotoUrl)
                            )

                            FirebaseFirestore.getInstance()
                                .collection("mascotas_perdidas")
                                .document(postId)
                                .update(updateData)
                                .addOnSuccessListener {
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    errorMensaje = "Error al actualizar los datos"
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Guardar Cambios", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    if (errorMensaje.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = errorMensaje, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}