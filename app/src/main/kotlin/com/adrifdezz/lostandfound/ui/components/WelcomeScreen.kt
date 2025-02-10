package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.skydoves.landscapist.glide.GlideImage
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.google.firebase.firestore.FirebaseFirestore
import com.adrifdezz.lostandfound.data.PostData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(navController: NavController) {
    val posts = remember { mutableStateListOf<PostData>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var expandedMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("mascotas_perdidas")
            .get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val post = document.toObject(PostData::class.java)
                    post.let {
                        it.id = document.id // Asigna el ID del documento al objeto PostData
                        posts.add(it)
                    }
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = e.localizedMessage ?: "Error al cargar las publicaciones."
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mascotas Perdidas",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = { expandedMenu = !expandedMenu }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu), // Asegúrate de tener un ícono llamado `ic_menu`
                            contentDescription = "Menú",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tus Publicaciones") },
                            onClick = {
                                expandedMenu = false
                                navController.navigate("your_posts_screen") // Navega a la pantalla de publicaciones del usuario
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión") },
                            onClick = {
                                expandedMenu = false
                                navController.navigate("auth_screen") // Cambiado a `auth_screen` para redirigir a la pantalla AuthScreen
                            }
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
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate("add_post_screen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Crear Publicación", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                } else if (posts.isEmpty()) {
                    Text(
                        text = "No hay publicaciones disponibles.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        posts.forEach { post ->
                            PostCard(post = post, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: PostData, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("post_details_screen/${post.id}") },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                imageModel = post.fotoUrl,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentDescription = "Imagen de ${post.nombre}"
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Nombre: ${post.nombre}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = "Localidad: ${post.localidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = "Fecha de pérdida: ${post.diaPerdido}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}