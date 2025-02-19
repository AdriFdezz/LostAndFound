package com.adrifdezz.lostandfound.ui.components

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.data.PostData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(postId: String, navController: NavController) {
    var post by remember { mutableStateOf<PostData?>(null) }
    val errorMessage by remember { mutableStateOf<String?>(null) }
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var ubicacion by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(postId) {
        try {
            val document = firestore.collection("mascotas_perdidas").document(postId).get().await()
            if (document.exists()) {
                val data = document.toObject(PostData::class.java)
                post = data?.copy(id = document.id)
                Log.d("PostDetailsScreen", "URL de la imagen cargada: ${data?.fotoUrl}")
            } else {
                Log.e("PostDetailsScreen", "El post con ID $postId no existe.")
            }
        } catch (e: Exception) {
            Log.e("PostDetailsScreen", "Error al cargar detalles del post: ${e.localizedMessage}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalles del Post",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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

            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (post == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                val data = post!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlideImage(
                        imageModel = data.fotoUrl,
                        contentDescription = "Imagen de ${data.nombre}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nombre: ${data.nombre}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Edad: ${data.edad}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Raza: ${data.raza}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Localidad: ${data.localidad}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Última Ubicación: ${data.ultimaUbicacion}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Día Perdido: ${data.diaPerdido}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Descripción: ${data.descripcion}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )

                    if (data.usuarioId != userId) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Text("Reportar Avistamiento", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
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
                    painter = painterResource(id = R.drawable.ic_visibility),
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Reportar Avistamiento",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            if (ubicacion.isNotBlank()) {
                                val avistamiento = mapOf(
                                    "postId" to postId,
                                    "reporterId" to userId,
                                    "usuarioId" to post?.usuarioId,
                                    "ubicacion" to ubicacion,
                                    "timestamp" to System.currentTimeMillis()
                                )

                                Log.d("Avistamiento", "Intentando guardar en Firestore: $avistamiento")

                                firestore.collection("avistamientos").add(avistamiento)
                                    .addOnSuccessListener {
                                        Log.d("Avistamiento", "Avistamiento guardado correctamente en Firestore")
                                        showDialog = false
                                        showConfirmationDialog = true
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("Avistamiento", "Error al guardar avistamiento: ${exception.message}")
                                        Toast.makeText(context, "Error al reportar avistamiento", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                Log.e("Avistamiento", "El campo de ubicación está vacío")
                                Toast.makeText(context, "La ubicación no puede estar vacía", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Enviar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showConfirmationDialog) {
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
                    text = "¡Avistamiento Enviado!",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tu reporte ha sido enviado exitosamente al dueño de la mascota.",
                    color = Color(0xFFB0BEC5),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showConfirmationDialog = false },
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
}