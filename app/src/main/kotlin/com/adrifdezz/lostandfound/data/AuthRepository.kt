package com.adrifdezz.lostandfound.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val autenticacionFirebase: FirebaseAuth = FirebaseAuth.getInstance()

    fun registrar(correo: String, contrasena: String, nombre: String, callback: (FirebaseUser?, String?) -> Unit) {
        val firestoreDB = FirebaseFirestore.getInstance()

        firestoreDB.collection("usuarios")
            .whereEqualTo("nombre", nombre)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    callback(null, "El nombre de usuario ya está en uso. Elige otro.")
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { tarea ->
                            if (tarea.isSuccessful) {
                                val usuario = tarea.result?.user
                                usuario?.let {
                                    val usuarioData = hashMapOf(
                                        "nombre" to nombre,
                                        "correo" to correo
                                    )
                                    firestoreDB.collection("usuarios").document(it.uid).set(usuarioData)
                                        .addOnSuccessListener {
                                            callback(usuario, null)
                                        }
                                        .addOnFailureListener { e ->
                                            callback(null, "Error al guardar datos en Firestore: ${e.message}")
                                        }
                                }
                            } else {
                                val mensajeError = traducirErrorFirebase(tarea.exception?.message)
                                callback(null, mensajeError)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(null, "Error al verificar nombre: ${e.message}")
            }
    }

    fun iniciarSesion(correo: String, contrasena: String, callback: (FirebaseUser?, String?) -> Unit) {
        autenticacionFirebase.signInWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    callback(autenticacionFirebase.currentUser, null)
                } else {
                    val mensajeError = traducirErrorFirebase(tarea.exception?.message)
                    callback(null, mensajeError)
                }
            }
    }

    fun recuperarContrasena(correo: String, callback: (Boolean, String) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(correo)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    println("DEBUG: Correo de recuperación enviado a $correo")
                    callback(true, "Correo de recuperación enviado")
                } else {
                    println("DEBUG: Error al enviar correo - ${tarea.exception?.message}")
                    val mensajeError = traducirErrorFirebase(tarea.exception?.message)
                    callback(false, mensajeError)
                }
            }
    }

    private fun traducirErrorFirebase(errorFirebase: String?): String {
        return when (errorFirebase) {
            "The email address is badly formatted." -> "La dirección de correo electrónico tiene un formato incorrecto."
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "No hay una cuenta registrada con este correo."
            "The password is invalid or the user does not have a password." -> "La contraseña es inválida o el usuario no tiene una contraseña."
            "The supplied auth credential is incorrect, malformed or has expired." -> "Las credenciales son incorrectas, vuelve a intentarlo."
            "The email address is already in use by another account." -> "Este correo ya está registrado. Usa otro correo o inicia sesión."
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Error de red. Verifica tu conexión a Internet."
            "We have blocked all requests from this device due to unusual activity. Try again later." -> "Hemos bloqueado las solicitudes desde este dispositivo debido a actividad inusual. Inténtalo más tarde."
            "Too many unsuccessful login attempts. Please try again later." -> "Demasiados intentos fallidos. Inténtalo más tarde."
            else -> "Ocurrió un error, intenta de nuevo."
        }
    }
}