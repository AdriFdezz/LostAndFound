package com.adrifdezz.lostandfound.ui.viewmodel

import androidx.lifecycle.*
import com.adrifdezz.lostandfound.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _usuario = MutableLiveData<FirebaseUser?>()
    val usuario: LiveData<FirebaseUser?> get() = _usuario

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _mensajeRecuperacion = MutableLiveData<String?>()
    val mensajeRecuperacion: LiveData<String?> get() = _mensajeRecuperacion

    val esInicioSesionExitoso = MutableLiveData(false)

    private val _lastRequestTime = MutableLiveData(0L)
    private val _remainingTime = MutableLiveData(0L)
    val remainingTime: LiveData<Long> get() = _remainingTime

    val cooldownTime = 60_000L
    private var isCooldownRunning = false

    fun registrar(correo: String, contrasena: String, nombre: String) {
        authRepository.registrar(correo, contrasena, nombre) { usuario, error ->
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

    fun verificarCorreoEnFirestore(correo: String, onResult: (Boolean, String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .whereEqualTo("correo", correo)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentos = task.result
                    if (!documentos.isEmpty) {
                        onResult(true, null)
                    } else {
                        onResult(false, "El correo no está registrado en nuestra base de datos.")
                    }
                } else {
                    val error = task.exception?.localizedMessage ?: "Error desconocido al verificar el correo."
                    onResult(false, error)
                }
            }
    }

    fun recuperarContrasena(correo: String) {
        if ((_remainingTime.value ?: 0) > 0) {
            return
        }

        authRepository.recuperarContrasena(correo) { exito, mensaje ->
            if (exito) {
                _mensajeRecuperacion.postValue("Correo de recuperación enviado")
            } else {
                _mensajeRecuperacion.postValue(mensaje)
            }

            if (exito) {
                val tiempoActual = System.currentTimeMillis()
                _lastRequestTime.postValue(tiempoActual)
                _remainingTime.value = cooldownTime / 1000
                iniciarContadorCooldown()
            }
        }
    }

    private fun iniciarContadorCooldown() {
        if (isCooldownRunning) return
        isCooldownRunning = true

        viewModelScope.launch {
            while ((_remainingTime.value ?: 0) > 0) {
                delay(1000L)
                val tiempoRestante = (_remainingTime.value ?: 1) - 1
                _remainingTime.postValue(if (tiempoRestante >= 0) tiempoRestante else 0)
            }
            isCooldownRunning = false
        }
    }

    fun calcularTiempoRestante() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - (_lastRequestTime.value ?: 0)

        if (elapsedTime < cooldownTime) {
            val tiempoRestante = (cooldownTime - elapsedTime) / 1000
            _remainingTime.value = tiempoRestante
            iniciarContadorCooldown()
        } else {
            _remainingTime.value = 0L
            _lastRequestTime.value = 0L
        }
    }

    fun iniciarTemporizadorSiEsNecesario() {
        if ((_remainingTime.value ?: 0) > 0 && !isCooldownRunning) {
            iniciarContadorCooldown()
        }
    }

    fun limpiarMensajeRecuperacion() {
        _mensajeRecuperacion.postValue(null)
    }

    fun actualizarTiempoRestante(nuevoTiempo: Long) {
        _remainingTime.postValue(nuevoTiempo)
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