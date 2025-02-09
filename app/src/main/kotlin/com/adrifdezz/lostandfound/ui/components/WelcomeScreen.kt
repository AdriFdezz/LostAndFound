package com.adrifdezz.lostandfound.ui.components

import com.skydoves.landscapist.glide.GlideImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.adrifdezz.lostandfound.data.PostData

@Composable
fun WelcomeScreen(navController: NavController) {
    val posts = remember { mutableStateListOf<PostData>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("mascotas_perdidas")
            .get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val post = document.toObject(PostData::class.java)
                    posts.add(post)
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = e.localizedMessage ?: "Error al cargar las publicaciones."
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Mascotas Perdidas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("add_post_screen") },
            modifier = Modifier.align(Alignment.End) // Alinea el botón a la derecha
        ) {
            Text(text = "Crear Publicación")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        } else if (posts.isEmpty()) {
            Text(
                text = "No hay publicaciones disponibles.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(posts.size) { index ->
                    val post = posts[index]
                    PostCard(post = post, navController = navController)
                }
            }
        }
    }
}

@Composable
fun PostCard(post: PostData, navController: NavController) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(250.dp)
            .clickable { navController.navigate("post_details_screen/${post.id}") },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                imageModel = post.imageUrl,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentDescription = "Imagen de ${post.nombre}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.nombre, style = MaterialTheme.typography.bodyLarge)
            Text(text = post.localidad, style = MaterialTheme.typography.bodySmall)
        }
    }
}