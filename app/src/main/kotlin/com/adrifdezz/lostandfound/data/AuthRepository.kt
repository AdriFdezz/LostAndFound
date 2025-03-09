package com.adrifdezz.lostandfound.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repositorio de autenticación que gestiona el registro, inicio de sesión,
 * cierre de sesión y recuperación de contraseña de los usuarios utilizando Firebase Authentication.
 */
class AuthRepository {

    private val autenticacionFirebase: FirebaseAuth = FirebaseAuth.getInstance() // Instancia de autenticación de Firebase

    /**
     * Registra un nuevo usuario en Firebase Authentication y almacena su información en Firestore.
     *
     * @param correo Dirección de correo electrónico del usuario.
     * @param contrasena Contraseña del usuario.
     * @param nombre Nombre de usuario.
     * @param callback Función de retorno con el usuario registrado o un mensaje de error.
     */
    fun registrar(correo: String, contrasena: String, nombre: String, callback: (FirebaseUser?, String?) -> Unit) {
        val firestoreDB = FirebaseFirestore.getInstance()

        // Verifica si el nombre de usuario ya está en uso en la colección "usuarios"
        firestoreDB.collection("usuarios")
            .whereEqualTo("nombre", nombre)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    callback(null, "El nombre de usuario ya está en uso. Elige otro.")
                } else {
                    // Si el nombre no está en uso, se registra el usuario en Firebase Authentication
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, contrasena)
                        .addOnCompleteListener { tarea ->
                            if (tarea.isSuccessful) {
                                val usuario = tarea.result?.user
                                usuario?.let {
                                    // Guarda la información del usuario en Firestore
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

    /**
     * Inicia sesión con un usuario existente en Firebase Authentication.
     *
     * @param correo Dirección de correo electrónico del usuario.
     * @param contrasena Contraseña del usuario.
     * @param callback Función de retorno con el usuario autenticado o un mensaje de error.
     */
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

    /**
     * Cierra la sesión del usuario actual en Firebase Authentication.
     */
    fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
    }

    /**
     * Envía un correo de restablecimiento de contraseña al usuario.
     *
     * @param correo Dirección de correo electrónico del usuario.
     * @param callback Función de retorno con un estado de éxito o un mensaje de error.
     */
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
}

/**
 * Traduce los mensajes de error de Firebase Authentication a mensajes más comprensibles para el usuario.
 *
 * @param errorFirebase Mensaje de error de Firebase en inglés.
 * @return Mensaje de error en español comprensible para el usuario.
 */
private fun traducirErrorFirebase(errorFirebase: String?): String {
    return when (errorFirebase) {
        "The email address is badly formatted." -> "La dirección de correo electrónico tiene un formato incorrecto."
        "There is no user record corresponding to this identifier. The user may have been deleted." ->
            "No hay un registro de usuario correspondiente a este identificador. El usuario puede haber sido eliminado."
        "The password is invalid or the user does not have a password." ->
            "La contraseña es inválida o el usuario no tiene una contraseña."
        "The supplied auth credential is incorrect, malformed or has expired." ->
            "Las credenciales son incorrectas, vuelve a intentarlo."
        "The email address is already in use by another account." ->
            "Este correo ya está registrado. Usa otro correo o inicia sesión."
        else -> "Credenciales incorrectas. Vuelve a intentarlo."
    }
}