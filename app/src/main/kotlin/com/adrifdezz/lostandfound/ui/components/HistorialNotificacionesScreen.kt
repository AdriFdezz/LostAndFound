package com.adrifdezz.lostandfound.ui.components

import android.util.Log
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.R
import com.adrifdezz.lostandfound.data.NotificacionAvistamiento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.skydoves.landscapist.glide.GlideImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla que muestra el historial de notificaciones de avistamientos de mascotas perdidas.
 * Permite visualizar los reportes de otros usuarios sobre las publicaciones del usuario autenticado.
 *
 * - Se conecta en tiempo real con Firestore mediante `addSnapshotListener()`.
 * - Si una publicación ha sido eliminada, sus avistamientos también se eliminan automáticamente.
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialNotificacionesScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid  // Obtiene el ID del usuario autenticado
    var notificaciones by remember { mutableStateOf(listOf<NotificacionAvistamiento>()) } // Lista de notificaciones
    var isLoading by remember { mutableStateOf(true) } // Estado de carga
    var errorMessage by remember { mutableStateOf("") } // Mensaje de error
    val firestore = FirebaseFirestore.getInstance() // Instancia de Firestore
    var listener: ListenerRegistration? by remember { mutableStateOf(null) } // Listener de Firestore

    /**
     * Se ejecuta cuando la pantalla se carga por primera vez. Configura un **listener en Firestore**
     * para actualizar las notificaciones en **tiempo real**.
     */
    LaunchedEffect(userId) {
        if (userId != null) {
            listener = firestore.collection("avistamientos")
                .whereEqualTo("usuarioId", userId) // Filtra por avistamientos del usuario autenticado
                .addSnapshotListener { querySnapshot, exception ->
                    if (exception != null) {
                        errorMessage = "Error al cargar notificaciones."
                        isLoading = false
                        return@addSnapshotListener
                    }

                    val nuevasNotificaciones = mutableListOf<NotificacionAvistamiento>()

                    querySnapshot?.documents?.forEach { doc ->
                        val postId = doc.getString("postId") ?: return@forEach
                        val ubicacion = doc.getString("ubicacion") ?: "Ubicación desconocida"
                        val timestamp = doc.getLong("timestamp") ?: 0L

                        firestore.collection("mascotas_perdidas").document(postId).get()
                            .addOnSuccessListener { postDoc ->
                                if (postDoc.exists()) {
                                    val fotoUrl = postDoc.getString("fotoUrl") ?: ""
                                    nuevasNotificaciones.add(
                                        NotificacionAvistamiento(
                                            id = doc.id,
                                            postId = postId,
                                            ubicacion = ubicacion,
                                            fotoUrl = fotoUrl,
                                            timestamp = timestamp
                                        )
                                    )
                                    notificaciones = nuevasNotificaciones
                                } else {
                                    // Si la publicación ha sido eliminada, se borra también el avistamiento
                                    firestore.collection("avistamientos").document(doc.id).delete()
                                        .addOnSuccessListener {
                                            Log.d("HistorialNotificaciones", "Avistamiento eliminado porque la publicación ya no existe.")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("HistorialNotificaciones", "Error eliminando avistamiento: ${e.message}")
                                        }
                                }
                            }
                    }
                    isLoading = false
                }
        } else {
            errorMessage = "No se encontró un usuario autenticado."
            isLoading = false
        }
    }

    /**
     * Se ejecuta cuando la pantalla se cierra para **eliminar el listener de Firestore**
     * y evitar fugas de memoria.
     */
    DisposableEffect(Unit) {
        onDispose {
            listener?.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notificaciones Avistamientos",
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (notificaciones.isEmpty()) {
                    Text(
                        text = "No tienes notificaciones.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        notificaciones.forEach { notificacion ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("post_details_screen/${notificacion.postId}") },
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlideImage(
                                        imageModel = notificacion.fotoUrl ?: "",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentDescription = "Imagen de la mascota"
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Avistamiento en:\n ${notificacion.ubicacion}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        val fechaLegible = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                                            timeZone = TimeZone.getTimeZone("Europe/Madrid")
                                        }.format(Date(notificacion.timestamp))

                                        Text(
                                            text = "Fecha: $fechaLegible",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}