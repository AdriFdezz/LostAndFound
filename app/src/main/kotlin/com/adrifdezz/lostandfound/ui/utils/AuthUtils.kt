package com.adrifdezz.lostandfound.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adrifdezz.lostandfound.R

fun validarContrasena(contrasena: String): Boolean {
    val tieneLongitudValida = contrasena.length >= 8
    val tieneNumero = contrasena.any { it.isDigit() }

    return tieneLongitudValida && tieneNumero
}

@Composable
fun BotonMostrarOcultarContrasena(mostrarContrasena: Boolean, onToggle: () -> Unit) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Image(
            painter = painterResource(id = if (mostrarContrasena) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
            contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
        )
    }
}