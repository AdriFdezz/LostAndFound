package com.adrifdezz.lostandfound.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrifdezz.lostandfound.data.AuthRepository
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    val isLoginSuccess = MutableLiveData(false)

    fun register(email: String, password: String) {
        authRepository.register(email, password) { user, error ->
            if (user != null) {
                _user.postValue(user)
            } else {
                _error.postValue(error)
            }
        }
    }

    fun login(email: String, password: String) {
        authRepository.login(email, password) { user, error ->
            if (user != null) {
                _user.postValue(user)
                isLoginSuccess.postValue(true) // Marcamos como éxito solo para login
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
