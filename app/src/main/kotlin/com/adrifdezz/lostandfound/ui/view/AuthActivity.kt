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
import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts

class AuthActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Log.e("AuthActivity", "El usuario denegó el permiso de notificaciones.")
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val factory = AuthViewModel.Factory(AuthRepository())
        val authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        setContent {
            val navController = rememberNavController()

            authViewModel.verificarSesionActiva()

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
            }

            authViewModel.esInicioSesionExitoso.observe(this) { esExitoso ->
                if (esExitoso) {
                    navController.navigate("welcome_screen") {
                        popUpTo("auth_screen") { inclusive = true }
                    }
                }
            }

            authViewModel.usuario.observe(this) { user ->
                if (user == null && authViewModel.sesionCerradaManualmente) {
                    navController.navigate("auth_screen") {
                        popUpTo("welcome_screen") { inclusive = true }
                    }
                }
            }
        }
    }
}