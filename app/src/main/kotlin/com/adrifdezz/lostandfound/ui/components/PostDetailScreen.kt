package com.adrifdezz.lostandfound.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.data.PostData
import com.google.firebase.firestore.FirebaseFirestore
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(postId: String, navController: NavController) {
    var post by remember { mutableStateOf<PostData?>(null) }
    val errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(postId) {
        try {
            val document = FirebaseFirestore.getInstance()
                .collection("mascotas_perdidas")
                .document(postId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.toObject(PostData::class.java)
                post = data
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
                title = { Text(text = "Detalles del Post") },
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
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else if (post == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val data = post!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlideImage(
                    imageModel = data.fotoUrl,
                    contentDescription = "Imagen de ${data.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Nombre: ${data.nombre}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Edad: ${data.edad}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Raza: ${data.raza}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Localidad: ${data.localidad}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Última Ubicación: ${data.ultimaUbicacion}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Día Perdido: ${data.diaPerdido}", style = MaterialTheme.typography.bodyLarge) // Nuevo campo agregado
                Text(text = "Descripción: ${data.descripcion}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}