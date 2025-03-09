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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.data.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.adrifdezz.lostandfound.data.PostData
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel

/**
 * Pantalla de bienvenida que muestra la lista de mascotas perdidas y permite al usuario
 * crear una nueva publicación, ver sus publicaciones, notificaciones o cerrar sesión.
 *
 * @param navController Controlador de navegación para gestionar las transiciones entre pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(navController: NavController) {
    val posts = remember { mutableStateListOf<PostData>() } // Lista de publicaciones disponibles
    var isLoading by remember { mutableStateOf(true) } // Estado de carga de las publicaciones
    var errorMessage by remember { mutableStateOf("") } // Mensaje de error en caso de fallo
    var expandedMenu by remember { mutableStateOf(false) } // Estado del menú desplegable

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(AuthRepository()))

    /**
     * Carga las publicaciones de mascotas perdidas desde Firestore al iniciarse la pantalla.
     */
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("mascotas_perdidas")
            .get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val post = document.toObject(PostData::class.java)
                    post.let {
                        it.id = document.id
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
            /**
             * Barra superior con título y menú de opciones.
             */
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
                    /**
                     * Menú desplegable con opciones del usuario.
                     */
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tus Publicaciones") },
                            onClick = {
                                expandedMenu = false
                                navController.navigate("tus_publicaciones_screen") // Navega a la pantalla de publicaciones del usuario
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Notificaciones") },
                            onClick = {
                                expandedMenu = false
                                navController.navigate("notificaciones_screen") // Nueva pantalla de notificaciones
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión") },
                            onClick = {
                                expandedMenu = false
                                authViewModel.cerrarSesion()
                                navController.navigate("auth_screen") {
                                    popUpTo("welcome_screen") { inclusive = true }
                                }
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

                /**
                 * Botón para crear una nueva publicación.
                 */
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

/**
 * Componente reutilizable que representa una tarjeta con la información de una publicación.
 *
 * @param post Datos de la publicación.
 * @param navController Controlador de navegación para redirigir a la pantalla de detalles.
 * @param isUserPost Indica si la publicación pertenece al usuario actual (para gestión de publicaciones propias).
 */
@Composable
fun PostCard(post: PostData, navController: NavController, isUserPost: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (isUserPost) {
                    navController.navigate("gestion_publicacion_screen/${post.id}")
                } else {
                    navController.navigate("post_details_screen/${post.id}")
                }
            },
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentDescription = "Imagen de ${post.nombre}"
            )

            Spacer(modifier = Modifier.width(16.dp))

            /**
             * Información de la publicación: nombre, localidad y fecha de pérdida.
             */
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