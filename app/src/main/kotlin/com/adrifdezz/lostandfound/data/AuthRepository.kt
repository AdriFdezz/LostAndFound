package com.adrifdezz.lostandfound.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(firebaseAuth.currentUser, null)
                } else {
                    callback(null, task.exception?.localizedMessage ?: "Error desconocido.")
                }
            }
    }

    fun login(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(firebaseAuth.currentUser, null)
                } else {
                    val errorMessage = translateFirebaseError(task.exception?.message)
                    callback(null, errorMessage)
                }
            }
    }

    private fun translateFirebaseError(firebaseError: String?): String {
        return when (firebaseError) {
            "The email address is badly formatted." -> "La dirección de correo electrónico tiene un formato incorrecto."
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "No hay un registro de usuario correspondiente a este identificador. El usuario puede haber sido eliminado."
            "The password is invalid or the user does not have a password." -> "La contraseña es inválida o el usuario no tiene una contraseña."
            "The supplied auth credential is incorrect, malformed or has expired." -> "Las credenciales son incorrectas, Vuelve a intentarlo."
            else -> "Credenciales incorrectas. Vuelve a intentarlo."
        }
    }
}
