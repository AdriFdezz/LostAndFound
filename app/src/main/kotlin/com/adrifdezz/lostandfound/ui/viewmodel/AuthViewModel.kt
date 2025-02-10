package com.adrifdezz.lostandfound.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.adrifdezz.lostandfound.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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

    fun publicarMascotaPerdida(
        nombre: String,
        edad: String,
        raza: String,
        localidad: String,
        ultimaUbicacion: String,
        descripcion: String,
        diaPerdido: String, // Nuevo parámetro para la fecha seleccionada
        fotoUri: Uri,
        callback: (Boolean, String?) -> Unit
    ) {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            Log.e("AuthViewModel", "❌ El usuario no está autenticado.")
            callback(false, "El usuario no está autenticado.")
            return
        }

        val userId = usuario.uid
        Log.d("AuthViewModel", "✅ Usuario autenticado con UID: $userId")

        val storageRef = FirebaseStorage.getInstance()
            .reference.child("fotos_mascotas/$userId/imagen_${System.currentTimeMillis()}.jpg")

        storageRef.putFile(fotoUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    Log.d("AuthViewModel", "URL de la imagen: $downloadUrl")
                    val mascotaData = hashMapOf(
                        "nombre" to nombre,
                        "edad" to edad,
                        "raza" to raza,
                        "localidad" to localidad,
                        "ultimaUbicacion" to ultimaUbicacion,
                        "descripcion" to descripcion,
                        "diaPerdido" to diaPerdido, // Guardar la fecha seleccionada
                        "fotoUrl" to downloadUrl.toString(),
                        "usuarioId" to userId,
                        "timestamp" to System.currentTimeMillis()
                    )

                    val firestore = FirebaseFirestore.getInstance()
                    val documentReference = firestore.collection("mascotas_perdidas").document()
                    val documentId = documentReference.id

                    mascotaData["id"] = documentId

                    documentReference.set(mascotaData)
                        .addOnSuccessListener {
                            Log.d("AuthViewModel", "✅ Datos guardados exitosamente en Firestore con ID: $documentId.")
                            callback(true, null)
                        }
                        .addOnFailureListener { e ->
                            Log.e("AuthViewModel", "❌ Error al guardar datos en Firestore: ${e.message}")
                            callback(false, "Error al guardar datos en Firestore: ${e.message}")
                        }
                }.addOnFailureListener { e ->
                    Log.e("AuthViewModel", "❌ Error al obtener la URL de la imagen: ${e.message}")
                    callback(false, "Error al obtener la URL de la imagen: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "❌ Error al subir la imagen: ${e.message}")
                callback(false, "Error al subir la imagen: ${e.message}")
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