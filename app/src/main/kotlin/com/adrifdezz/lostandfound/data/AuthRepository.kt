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

    fun recuperarContrasena(correo: String, callback: (Boolean, String?) -> Unit) {
        autenticacionFirebase.sendPasswordResetEmail(correo)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    callback(true, null)
                } else {
                    val mensajeError = traducirErrorFirebase(tarea.exception?.message)
                    callback(false, mensajeError)
                }
            }
    }

    private fun traducirErrorFirebase(errorFirebase: String?): String {
        return when (errorFirebase) {
            "The email address is badly formatted." -> "La dirección de correo electrónico tiene un formato incorrecto."
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "No hay un registro de usuario correspondiente a este identificador. El usuario puede haber sido eliminado."
            "The password is invalid or the user does not have a password." -> "La contraseña es inválida o el usuario no tiene una contraseña."
            "The supplied auth credential is incorrect, malformed or has expired." -> "Las credenciales son incorrectas, vuelve a intentarlo."
            "The email address is already in use by another account." -> "Este correo ya está registrado. Usa otro correo o inicia sesión."
            else -> "Credenciales incorrectas. Vuelve a intentarlo."
        }
    }
}