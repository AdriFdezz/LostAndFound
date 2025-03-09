package com.adrifdezz.lostandfound.data

/**
 * Representa los datos de una publicación sobre una mascota perdida.
 *
 * Esta clase se usa para almacenar y recuperar información sobre una publicación
 * en la base de datos de Firestore.
 *
 * @property id Identificador único de la publicación.
 * @property nombre Nombre de la mascota.
 * @property edad Edad de la mascota.
 * @property raza Raza de la mascota.
 * @property localidad Localidad donde se perdió la mascota.
 * @property ultimaUbicacion Última ubicación vista de la mascota.
 * @property descripcion Descripción adicional o datos de interés sobre la mascota.
 * @property fotoUrl URL de la imagen de la mascota.
 * @property diaPerdido Fecha en la que la mascota se perdió.
 * @property usuarioId Identificador único del usuario que creó la publicación.
 */
data class PostData(
    var id: String = "",
    val nombre: String = "",
    val edad: String = "",
    val raza: String = "",
    val localidad: String = "",
    val ultimaUbicacion: String = "",
    val descripcion: String = "",
    val fotoUrl: String = "",
    val diaPerdido: String = "",
    val usuarioId: String = ""
)