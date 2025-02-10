package com.adrifdezz.lostandfound.data

data class PostData(
    var id: String = "",
    val nombre: String = "",
    val edad: String = "",
    val raza: String = "",
    val localidad: String = "",
    val ultimaUbicacion: String = "",
    val descripcion: String = "",
    val fotoUrl: String = ""
)
