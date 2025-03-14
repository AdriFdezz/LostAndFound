package com.adrifdezz.lostandfound.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.navigation.NavController
import com.adrifdezz.lostandfound.data.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la autenticación de usuarios, recuperación de contraseña
 * y publicación de mascotas perdidas en Firebase.
 *
 * @param authRepository Repositorio de autenticación para interactuar con Firebase Authentication.
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _usuario = MutableLiveData<FirebaseUser?>()
    val usuario: LiveData<FirebaseUser?> get() = _usuario

    private val _mostrarAlertaCorreoVerificado = MutableLiveData(false)
    val mostrarAlertaCorreoVerificado: LiveData<Boolean> get() = _mostrarAlertaCorreoVerificado
    private val _mostrarAlertaCuentaEliminada = MutableLiveData(false)
    val mostrarAlertaCuentaEliminada: LiveData<Boolean> get() = _mostrarAlertaCuentaEliminada

    init {
        _usuario.value = FirebaseAuth.getInstance().currentUser
        observarCambioCorreo()
    }

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _mensajeRecuperacion = MutableLiveData<String?>()
    val mensajeRecuperacion: LiveData<String?> get() = _mensajeRecuperacion

    val esInicioSesionExitoso = MutableLiveData(false)

    var sesionCerradaManualmente = false

    private val _lastRequestTime = MutableLiveData(0L)
    private val _remainingTime = MutableLiveData(0L)
    val remainingTime: LiveData<Long> get() = _remainingTime

    val cooldownTime = 60_000L
    private var isCooldownRunning = false

    /**
     * Registra un nuevo usuario en Firebase Authentication y guarda sus datos en Firestore.
     *
     * @param correo Correo del usuario.
     * @param contrasena Contraseña del usuario.
     * @param nombre Nombre del usuario.
     */
    fun registrar(correo: String, contrasena: String, nombre: String) {
        authRepository.registrar(correo, contrasena, nombre) { usuario, error ->
            if (usuario != null) {
                _usuario.postValue(usuario)
            } else {
                _error.postValue(error)
            }
        }
    }

    /**
     * Inicia sesión con Firebase Authentication.
     *
     * @param correo Correo del usuario.
     * @param contrasena Contraseña del usuario.
     */
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

    /**
     * Actualiza el correo electrónico del usuario autenticado en Firebase Authentication y Firestore.
     *
     * Este metodo verifica si el nuevo correo ya está en uso en Firestore. Si no está en uso,
     * solicita la autenticación del usuario con su contraseña actual para proceder con la actualización.
     * Luego, se envía un correo de verificación al nuevo correo y se actualiza el correo en Firestore.
     * Finalmente, cierra la sesión y redirige a la pantalla de autenticación.
     *
     * @param nuevoCorreo Nuevo correo electrónico del usuario.
     * @param contrasenaActual Contraseña actual del usuario para la reautenticación.
     * @param navController Controlador de navegación para manejar la redirección tras la actualización.
     * @param callback Función de retorno que indica el resultado de la operación. Devuelve `true` si la actualización fue exitosa o `false` con un mensaje de error en caso de fallo.
     */
    fun actualizarCorreo(nuevoCorreo: String, contrasenaActual: String, navController: NavController, callback: (Boolean, String) -> Unit) {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        // Verificar si el correo ya está en uso en Firestore
        db.collection("usuarios").whereEqualTo("correo", nuevoCorreo).get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    callback(false, "Este correo ya está en uso. Elige otro.")
                } else {
                    // Si el correo no está en uso, proceder con la autenticación
                    val credential = EmailAuthProvider.getCredential(usuario.email!!, contrasenaActual)

                    usuario.reauthenticate(credential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            usuario.verifyBeforeUpdateEmail(nuevoCorreo).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Actualizar el correo en Firestore
                                    db.collection("usuarios").document(usuario.uid)
                                        .update("correo", nuevoCorreo)
                                        .addOnSuccessListener {
                                            callback(true, "Se ha enviado un correo de verificación a $nuevoCorreo. Confírmalo para completar el cambio.")
                                            cerrarSesionYRedirigir(navController, false) // Cierra sesión y redirige a la pantalla de login
                                        }
                                        .addOnFailureListener {
                                            callback(false, "Error al actualizar el correo en Firestore: ${it.message}")
                                        }
                                } else {
                                    callback(false, "Error al iniciar la actualización del correo: ${updateTask.exception?.message}")
                                }
                            }
                        } else {
                            callback(false, "Error de autenticación: ${authTask.exception?.message}")
                        }
                    }
                }
            }
            .addOnFailureListener {
                callback(false, "Error al verificar el correo en la base de datos: ${it.message}")
            }
    }

    /**
     * Observa los cambios en el estado de autenticación del usuario y actualiza su correo en Firestore.
     *
     * Este metodo agrega un `AuthStateListener` a Firebase Authentication para detectar cambios en el usuario autenticado.
     * Si el usuario cambia, se obtiene su nuevo correo y se actualiza en Firestore.
     */
    private fun observarCambioCorreo() {
        val auth = FirebaseAuth.getInstance()

        auth.addAuthStateListener { firebaseAuth ->
            val usuario = firebaseAuth.currentUser
            if (usuario != null) {
                val nuevoCorreo = usuario.email ?: return@addAuthStateListener

                // Actualizar el correo en Firestore
                FirebaseFirestore.getInstance().collection("usuarios")
                    .document(usuario.uid)
                    .update("correo", nuevoCorreo)
                    .addOnSuccessListener {
                        Log.d("AuthViewModel", "Correo actualizado en Firestore: $nuevoCorreo")
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthViewModel", "Error al actualizar el correo en Firestore", e)
                    }
            }
        }
    }

    /**
     * Activa la alerta de correo verificado.
     *
     * Esta función actualiza el valor de `_mostrarAlertaCorreoVerificado` a `true`,
     * lo que indica que el correo del usuario ha sido verificado.
     */
    fun activarAlertaCorreoVerificado() {
        _mostrarAlertaCorreoVerificado.postValue(true)
    }

    /**
     * Desactiva la alerta de correo verificado.
     *
     * Esta función actualiza el valor de `_mostrarAlertaCorreoVerificado` a `false`,
     * ocultando la alerta de correo verificado.
     */
    fun desactivarAlertaCorreoVerificado() {
        _mostrarAlertaCorreoVerificado.postValue(false)
    }

    /**
     * Activa la alerta de cuenta eliminada.
     *
     * Esta función actualiza el valor de `_mostrarAlertaCuentaEliminada` a `true`,
     * indicando que la cuenta del usuario ha sido eliminada.
     */
    private fun activarAlertaCuentaEliminada() {
        _mostrarAlertaCuentaEliminada.postValue(true)
    }

    /**
     * Desactiva la alerta de cuenta eliminada.
     *
     * Esta función actualiza el valor de `_mostrarAlertaCuentaEliminada` a `false`,
     * ocultando la alerta de cuenta eliminada.
     */
    fun desactivarAlertaCuentaEliminada() {
        _mostrarAlertaCuentaEliminada.postValue(false)
    }

    /**
     * Cierra la sesión del usuario y redirige a la pantalla de autenticación.
     *
     * Esta función cierra la sesión del usuario en Firebase Authentication, actualiza el estado del usuario en el ViewModel
     * y muestra una alerta dependiendo de si la cuenta fue eliminada o si solo se cerró la sesión.
     * Luego, redirige a la pantalla de autenticación.
     *
     * @param navController Controlador de navegación para manejar la redirección.
     * @param esCuentaEliminada Indica si la cuenta ha sido eliminada (`true`) o si solo se cerró la sesión (`false`).
     */
    private fun cerrarSesionYRedirigir(navController: NavController, esCuentaEliminada: Boolean) {
        FirebaseAuth.getInstance().signOut()
        _usuario.postValue(null)

        if (esCuentaEliminada) {
            activarAlertaCuentaEliminada()
        } else {
            activarAlertaCorreoVerificado()
        }

        navController.navigate("auth_screen") {
            popUpTo(0) { inclusive = true }
        }
    }

    /**
     * Recarga los datos del usuario autenticado y actualiza su correo en Firestore.
     *
     * Esta función recarga los datos del usuario en Firebase Authentication para obtener información actualizada.
     * Si la recarga es exitosa, obtiene el correo actualizado y lo almacena en Firestore.
     * En caso de error, se registra un mensaje en el log.
     */
    fun refrescarUsuarioYActualizarCorreo() {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return

        usuario.reload().addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                val nuevoCorreo = usuario.email ?: return@addOnCompleteListener

                FirebaseFirestore.getInstance().collection("usuarios")
                    .document(usuario.uid)
                    .update("correo", nuevoCorreo)
                    .addOnSuccessListener {
                        Log.d("AuthViewModel", "Correo actualizado en Firestore: $nuevoCorreo")
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthViewModel", "Error al actualizar el correo en Firestore", e)
                    }
            } else {
                Log.e("AuthViewModel", "Error al recargar usuario: ${reloadTask.exception?.message}")
            }
        }
    }

    /**
     * Actualiza el nombre del usuario en Firestore.
     *
     * Esta función verifica si el nuevo nombre ya está en uso en la base de datos.
     * Si el nombre está disponible, lo actualiza en el perfil del usuario en Firestore.
     * En caso de éxito o error, se invoca el `callback` con el resultado de la operación.
     *
     * @param nuevoNombre Nuevo nombre del usuario.
     * @param callback Función de retorno que indica el resultado de la operación. Devuelve `true` y un mensaje de éxito si la actualización fue correcta
     * o `false` con un mensaje de error en caso de fallo.
     */
    fun actualizarNombre(nuevoNombre: String, callback: (Boolean, String) -> Unit) {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val userId = usuario.uid
        val db = FirebaseFirestore.getInstance()

        if (nuevoNombre.isBlank()) {
            callback(false, "El nombre no puede estar vacío.")
            return
        }
        // Verificar si el nombre ya está en uso
        db.collection("usuarios").whereEqualTo("nombre", nuevoNombre).get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    callback(false, "Este nombre ya está en uso. Elige otro.")
                } else {
                    // Si el nombre no esta en uso se actualiza
                    db.collection("usuarios").document(userId)
                        .update("nombre", nuevoNombre)
                        .addOnSuccessListener {
                            callback(true, "Nombre actualizado correctamente.")
                        }
                        .addOnFailureListener {
                            callback(false, "Error al actualizar el nombre: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                callback(false, "Error al verificar el nombre en la base de datos: ${it.message}")
            }
    }

    /**
     * Elimina la cuenta del usuario autenticado en Firebase.
     *
     * Esta función realiza varios pasos para eliminar completamente la cuenta del usuario:
     * - Reautentica al usuario con su contraseña actual.
     * - Elimina los avistamientos asociados al usuario en Firestore.
     * - Elimina las publicaciones del usuario, incluyendo sus imágenes almacenadas en Firebase Storage.
     * - Elimina el perfil del usuario en Firestore.
     * - Elimina la cuenta del usuario en Firebase Authentication.
     * - Cierra la sesión y redirige a la pantalla de autenticación.
     *
     * @param contrasenaActual Contraseña actual del usuario para la reautenticación.
     * @param navController Controlador de navegación para manejar la redirección tras la eliminación.
     * @param callback Función de retorno que indica el resultado de la operación. Devuelve `true` con un mensaje de éxito si la cuenta se eliminó correctamente,
     * o `false` con un mensaje de error si ocurrió algún problema.
     */
    fun eliminarCuenta(contrasenaActual: String, navController: NavController, callback: (Boolean, String) -> Unit) {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val credential = EmailAuthProvider.getCredential(usuario.email!!, contrasenaActual)

        usuario.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                val userId = usuario.uid
                val db = FirebaseFirestore.getInstance()
                val storage = FirebaseStorage.getInstance()

                // Eliminar avistamientos del usuario
                db.collection("avistamientos").whereEqualTo("usuarioId", userId).get()
                    .addOnSuccessListener { avistamientos ->
                        avistamientos.forEach { it.reference.delete() }
                    }
                    .addOnFailureListener { callback(false, "Error al eliminar avistamientos del usuario.") }
                    .addOnCompleteListener {

                        // Eliminar publicaciones del usuario
                        db.collection("mascotas_perdidas").whereEqualTo("usuarioId", userId).get()
                            .addOnSuccessListener { documentos ->
                                documentos.forEach { doc ->
                                    val postId = doc.id
                                    val fotoUrl = doc.getString("fotoUrl")

                                    // Eliminar imagen del post en Firebase Storage
                                    fotoUrl?.let { storage.getReferenceFromUrl(it).delete() }

                                    // Eliminar avistamientos relacionados con la publicación
                                    db.collection("avistamientos").whereEqualTo("postId", postId).get()
                                        .addOnSuccessListener { avistamientos ->
                                            avistamientos.forEach { it.reference.delete() }
                                        }

                                    // Eliminar publicación
                                    doc.reference.delete()
                                }
                            }
                            .addOnFailureListener { callback(false, "Error al eliminar publicaciones del usuario.") }
                            .addOnCompleteListener {

                                // Eliminar el perfil del usuario en Firestore
                                db.collection("usuarios").document(userId).delete()
                                    .addOnSuccessListener {
                                        // Ahora eliminamos la cuenta de Firebase Authentication y redirigimos
                                        usuario.delete().addOnSuccessListener {
                                            callback(true, "Cuenta eliminada correctamente.")
                                            cerrarSesionYRedirigir(navController, true) // Redirige con la alerta de cuenta eliminada
                                        }.addOnFailureListener {
                                            callback(false, "Error al eliminar cuenta: ${it.message}")
                                        }
                                    }.addOnFailureListener {
                                        callback(false, "Error al eliminar perfil del usuario.")
                                    }
                            }
                    }
            } else {
                callback(false, "Error de autenticación: ${authTask.exception?.message}")
            }
        }
    }

    /**
     * Verifica si el correo ingresado está registrado en Firestore.
     *
     * @param correo Correo a verificar.
     * @param onResult Callback con `true` si el correo existe o `false` con un mensaje de error.
     */
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

    /**
     * Envía un correo de recuperación de contraseña y activa un temporizador de espera.
     *
     * @param correo Correo del usuario que solicita la recuperación.
     */
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

    /**
     * Inicia un temporizador para el tiempo de espera antes de permitir otra recuperación de contraseña.
     */
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

    /**
     * Calcula el tiempo restante del cooldown para recuperar la contraseña.
     *
     * - Obtiene el tiempo actual y lo compara con la última solicitud de recuperación.
     * - Si el cooldown sigue activo, actualiza el tiempo restante y reinicia el temporizador.
     * - Si el cooldown ha terminado, resetea los valores de tiempo restante y la última solicitud.
     */
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

    /**
     * Inicia el temporizador de cooldown si es necesario.
     *
     * - Verifica si hay tiempo restante y si el cooldown aún no está corriendo.
     * - Si el temporizador no está en ejecución y hay tiempo restante, lo inicia.
     */
    fun iniciarTemporizadorSiEsNecesario() {
        if ((_remainingTime.value ?: 0) > 0 && !isCooldownRunning) {
            iniciarContadorCooldown()
        }
    }

    /**
     * Limpia el mensaje de recuperación de contraseña.
     *
     * - Se usa para resetear el estado del mensaje de éxito o error
     *   después de un intento de recuperación de contraseña.
     */
    fun limpiarMensajeRecuperacion() {
        _mensajeRecuperacion.postValue(null)
    }

    /**
     * Actualiza manualmente el tiempo restante del cooldown de recuperación de contraseña.
     *
     * @param nuevoTiempo Nuevo valor del tiempo restante en segundos.
     */
    fun actualizarTiempoRestante(nuevoTiempo: Long) {
        _remainingTime.postValue(nuevoTiempo)
    }

    /**
     * Publica un nuevo reporte de mascota perdida en Firestore con una imagen subida a Firebase Storage.
     *
     * @param nombre Nombre de la mascota.
     * @param edad Edad de la mascota.
     * @param raza Raza de la mascota.
     * @param localidad Localidad donde se perdió la mascota.
     * @param ultimaUbicacion Última ubicación vista.
     * @param descripcion Descripción de la mascota.
     * @param diaPerdido Fecha en que se perdió la mascota.
     * @param fotoUri URI de la foto de la mascota.
     * @param callback Callback con `true` si la operación fue exitosa, `false` con mensaje de error en caso contrario.
     */
    fun publicarMascotaPerdida(
        nombre: String,
        edad: String,
        raza: String,
        localidad: String,
        ultimaUbicacion: String,
        descripcion: String,
        diaPerdido: String,
        fotoUri: Uri,
        callback: (Boolean, String?) -> Unit
    ) {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            Log.e("AuthViewModel", "El usuario no está autenticado.")
            callback(false, "El usuario no está autenticado.")
            return
        }

        val userId = usuario.uid
        Log.d("AuthViewModel", "Usuario autenticado con UID: $userId")

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
                        "diaPerdido" to diaPerdido,
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
                            Log.d("AuthViewModel", "Datos guardados exitosamente en Firestore con ID: $documentId.")
                            callback(true, null)
                        }
                        .addOnFailureListener { e ->
                            Log.e("AuthViewModel", "Error al guardar datos en Firestore: ${e.message}")
                            callback(false, "Error al guardar datos en Firestore: ${e.message}")
                        }
                }.addOnFailureListener { e ->
                    Log.e("AuthViewModel", "Error al obtener la URL de la imagen: ${e.message}")
                    callback(false, "Error al obtener la URL de la imagen: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Error al subir la imagen: ${e.message}")
                callback(false, "Error al subir la imagen: ${e.message}")
            }
    }

    /**
     * Verifica si hay una sesión de usuario activa en Firebase Authentication.
     */
    fun verificarSesionActiva() {
        val usuarioActual = FirebaseAuth.getInstance().currentUser
        _usuario.postValue(usuarioActual)
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun cerrarSesion() {
        sesionCerradaManualmente = true
        authRepository.cerrarSesion()
        _usuario.postValue(null)
    }

    /**
     * Fábrica para la creación del ViewModel `AuthViewModel`.
     *
     * @param repository Repositorio de autenticación.
     */
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