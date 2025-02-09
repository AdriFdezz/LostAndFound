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
import com.adrifdezz.lostandfound.ui.components.AuthScreen
import com.adrifdezz.lostandfound.ui.components.PasswordRecoveryScreen
import com.adrifdezz.lostandfound.ui.components.WelcomeScreen
import com.adrifdezz.lostandfound.ui.components.AddPostScreen
import com.adrifdezz.lostandfound.ui.components.PostDetailsScreen

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModel.Factory(AuthRepository())
        )[AuthViewModel::class.java]

        setContent {
            val navController = rememberNavController()

            // Define la pantalla inicial basándote en el estado del usuario autenticado
            val startDestination = if (authViewModel.usuario.value != null) "welcome_screen" else "auth_screen"

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
            }

            // Observa los cambios en `esInicioSesionExitoso` para redirigir al usuario
            authViewModel.esInicioSesionExitoso.observe(this) { esExitoso ->
                if (esExitoso) {
                    navController.navigate("welcome_screen") {
                        popUpTo("auth_screen") { inclusive = true }
                    }
                }
            }

            // Observa los cambios en el usuario para garantizar la navegación adecuada
            authViewModel.usuario.observe(this) { user ->
                if (user == null && navController.currentDestination?.route != "auth_screen") {
                    // Si el usuario no está autenticado, redirige a la pantalla de autenticación
                    navController.navigate("auth_screen") {
                        popUpTo("welcome_screen") { inclusive = true }
                    }
                }
            }
        }
    }
}