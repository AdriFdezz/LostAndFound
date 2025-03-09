package com.adrifdezz.lostandfound.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.adrifdezz.lostandfound.data.AuthRepository
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import com.adrifdezz.lostandfound.ui.components.*

/**
 * `AuthActivity` es la actividad principal encargada de gestionar la autenticación del usuario
 * y la navegación entre las diferentes pantallas de la aplicación.
 *
 * Esta actividad inicializa el `AuthViewModel` y configura la navegación usando `NavHost`.
 */
class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crea una instancia del AuthViewModel con su fábrica personalizada
        val factory = AuthViewModel.Factory(AuthRepository())
        val authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        setContent {
            val navController = rememberNavController()

            // Verifica si hay una sesión activa al iniciar la aplicación
            authViewModel.verificarSesionActiva()

            // Determina la pantalla de inicio en función de si hay un usuario autenticado o no
            val startDestination = if (authViewModel.usuario.value != null) "welcome_screen" else "auth_screen"

            /**
             * `NavHost` define la estructura de navegación de la aplicación.
             * Cada `composable` representa una pantalla a la que se puede navegar.
             */
            NavHost(navController = navController, startDestination = startDestination) {
                composable("auth_screen") {
                    AuthScreen(authViewModel = authViewModel, navController = navController)
                }
                composable("welcome_screen") {
                    WelcomeScreen(navController = navController)
                }
                composable("add_post_screen") {
                    AddPostScreen(authViewModel = authViewModel, navController = navController)
                }
                composable("password_recovery_screen") {
                    PasswordRecoveryScreen(authViewModel = authViewModel) {
                        navController.navigate("auth_screen")
                    }
                }
                composable(
                    route = "post_details_screen/{postId}",
                    arguments = listOf(navArgument("postId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: ""
                    PostDetailsScreen(postId = postId, navController = navController)
                }
                composable("tus_publicaciones_screen") {
                    TusPublicacionesScreen(navController = navController)
                }
                composable(
                    route = "gestion_publicacion_screen/{postId}",
                    arguments = listOf(navArgument("postId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                    GestionPublicacionScreen(postId, navController)
                }
                composable(
                    route = "edit_post_screen/{postId}",
                    arguments = listOf(navArgument("postId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                    EditPostScreen(postId, navController)
                }
                composable("notificaciones_screen") {
                    HistorialNotificacionesScreen(navController = navController)
                }
            }

            /**
             * Observa el estado de inicio de sesión exitoso.
             * Si el usuario inicia sesión correctamente, se navega a la pantalla de bienvenida.
             */
            authViewModel.esInicioSesionExitoso.observe(this) { esExitoso ->
                if (esExitoso) {
                    navController.navigate("welcome_screen") {
                        popUpTo("auth_screen") { inclusive = true } // Elimina `auth_screen` del stack de navegación
                    }
                }
            }

            /**
             * Observa el estado del usuario autenticado.
             * Si el usuario es `null` y la sesión se cerró manualmente, se redirige a la pantalla de autenticación.
             */
            authViewModel.usuario.observe(this) { user ->
                if (user == null && authViewModel.sesionCerradaManualmente) {
                    navController.navigate("auth_screen") {
                        popUpTo("welcome_screen") { inclusive = true } // Elimina `welcome_screen` del stack de navegación
                    }
                }
            }
        }
    }
}