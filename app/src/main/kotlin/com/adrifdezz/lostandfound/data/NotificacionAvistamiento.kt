package com.adrifdezz.lostandfound.data

/**
 * Representa una notificación de avistamiento en la aplicación.
 *
 * @property id Identificador único del avistamiento en Firestore.
 * @property postId ID de la publicación de la mascota perdida asociada al avistamiento.
 * @property ubicacion Ubicación donde se realizó el avistamiento.
 * @property fotoUrl URL de la imagen de la mascota avistada (opcional).
 * @property timestamp Fecha y hora en formato UNIX del avistamiento.
 */
data class NotificacionAvistamiento(
    val id: String = "",
    val postId: String = "",
    val ubicacion: String = "",
    val fotoUrl: String? = null,
    val timestamp: Long = 0L
)