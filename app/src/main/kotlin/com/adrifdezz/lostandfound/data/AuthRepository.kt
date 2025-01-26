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
                    callback(null, task.exception?.message)
                }
            }
    }

    fun login(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(firebaseAuth.currentUser, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }
}
