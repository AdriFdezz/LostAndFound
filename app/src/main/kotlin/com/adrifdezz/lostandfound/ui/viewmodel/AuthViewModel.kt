package com.adrifdezz.lostandfound.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.adrifdezz.lostandfound.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
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

    private val cooldownTime = 60_000L

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

    fun recuperarContrasena(correo: String) {
        if ((_remainingTime.value ?: 0) > 0) {
            return
        }

        authRepository.recuperarContrasena(correo) { exito, mensaje ->
            Log.d("DEBUG", "🛑 Estado del mensajeRecuperacion antes: ${_mensajeRecuperacion.value}")
            if (exito) {
                _mensajeRecuperacion.postValue("Correo de recuperación enviado")
            } else {
                _mensajeRecuperacion.postValue(mensaje)
            }
            Log.d("DEBUG", "✅ Estado del mensajeRecuperacion después: ${_mensajeRecuperacion.value}")

            if (exito) {
                val tiempoActual = System.currentTimeMillis()
                _lastRequestTime.postValue(tiempoActual)
                _remainingTime.value = cooldownTime / 1000
                println("✅ Correo enviado. Iniciando cooldown: ${_remainingTime.value}")
                iniciarContadorCooldown()
            }
        }
    }

    private fun iniciarContadorCooldown() {
        viewModelScope.launch {
            while ((_remainingTime.value ?: 0) > 0) {
                delay(1000L)
                val tiempoRestante = (_remainingTime.value ?: 1) - 1
                _remainingTime.postValue(if (tiempoRestante >= 0) tiempoRestante else 0)
            }
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
        if ((_remainingTime.value ?: 0) > 0) {
            return
        }

        viewModelScope.launch {
            while ((_remainingTime.value ?: 0) > 0) {
                delay(1000L)
                val nuevoTiempo = (_remainingTime.value ?: 1) - 1
                _remainingTime.postValue(if (nuevoTiempo >= 0) nuevoTiempo else 0)
            }
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