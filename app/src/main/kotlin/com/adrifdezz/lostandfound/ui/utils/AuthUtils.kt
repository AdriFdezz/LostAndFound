package com.adrifdezz.lostandfound.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adrifdezz.lostandfound.R

/**
 * Valida que una contraseña cumpla con los requisitos mínimos de seguridad.
 *
 * La contraseña es válida si:
 * - Tiene al menos 8 caracteres.
 * - Contiene al menos un número.
 *
 * @param contrasena La contraseña a validar.
 * @return `true` si la contraseña es válida, `false` en caso contrario.
 */
fun validarContrasena(contrasena: String): Boolean {
    val tieneLongitudValida = contrasena.length >= 8
    val tieneNumero = contrasena.any { it.isDigit() }

    return tieneLongitudValida && tieneNumero
}

/**
 * Botón para mostrar u ocultar la contraseña en un campo de entrada.
 *
 * Este botón cambia el icono dependiendo del estado actual:
 * - Si `mostrarContrasena` es `true`, muestra un icono de "ocultar contraseña".
 * - Si `mostrarContrasena` es `false`, muestra un icono de "mostrar contraseña".
 *
 * @param mostrarContrasena Estado actual de visibilidad de la contraseña.
 * @param onToggle Acción a ejecutar cuando se presiona el botón (cambia el estado).
 */
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