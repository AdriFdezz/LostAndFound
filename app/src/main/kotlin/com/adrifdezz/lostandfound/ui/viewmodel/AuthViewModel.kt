package com.adrifdezz.lostandfound.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrifdezz.lostandfound.data.AuthRepository
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _usuario = MutableLiveData<FirebaseUser?>()
    val usuario: LiveData<FirebaseUser?> get() = _usuario

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    val esInicioSesionExitoso = MutableLiveData(false)

    fun registrar(correo: String, contrasena: String, nombre: String) {
        authRepository.registrar(correo, contrasena) { usuario, error ->
            if (usuario != null) {
                _usuario.postValue(usuario)
            } else {
                _error.postValue(error)
            }
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        authRepository.iniciarSesion(correo, contrasena) { usuario, error ->
            if (usuario != null) {
                _usuario.postValue(usuario)
                esInicioSesionExitoso.postValue(true)
            } else {
                _error.postValue(error)
            }
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida")
        }
    }
}
