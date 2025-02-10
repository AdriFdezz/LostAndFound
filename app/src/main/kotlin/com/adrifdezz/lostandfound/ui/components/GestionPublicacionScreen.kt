package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.data.PostData
import com.google.firebase.firestore.FirebaseFirestore
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPublicacionScreen(postId: String, navController: NavController) {
    var post by remember { mutableStateOf<PostData?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(postId) {
        FirebaseFirestore.getInstance()
            .collection("mascotas_perdidas")
            .document(postId)
            .get()
            .addOnSuccessListener { document ->
                post = document.toObject(PostData::class.java)?.apply { id = document.id }
                isLoading.value = false
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Publicación", color = Color.White) },
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

            if (isLoading.value) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                post?.let { data ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Imagen principal
                        GlideImage(
                            imageModel = data.fotoUrl,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentDescription = "Imagen de la publicación"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Información de la publicación
                        Text(
                            text = "Nombre: ${data.nombre}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Edad: ${data.edad}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Raza: ${data.raza}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Localidad: ${data.localidad}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Fecha de pérdida: ${data.diaPerdido}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Última ubicación: ${data.ultimaUbicacion}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Descripción: ${data.descripcion.ifBlank { "Sin descripción" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para editar publicación
                        Button(
                            onClick = { navController.navigate("edit_post_screen/$postId") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Editar Publicación", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Botón para cerrar publicación
                        Button(
                            onClick = {
                                FirebaseFirestore.getInstance()
                                    .collection("mascotas_perdidas")
                                    .document(postId)
                                    .delete()
                                    .addOnSuccessListener {
                                        navController.popBackStack()
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cerrar Publicación", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}