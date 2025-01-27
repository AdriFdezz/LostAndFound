package com.adrifdezz.lostandfound.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.*
import com.adrifdezz.lostandfound.data.AuthRepository
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import com.adrifdezz.lostandfound.ui.components.AuthScreen
import com.adrifdezz.lostandfound.ui.components.WelcomeScreen

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModel.Factory(AuthRepository())
        )[AuthViewModel::class.java]

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "auth_screen") {
                composable("auth_screen") {
                    AuthScreen(authViewModel = authViewModel)
                }
                composable("welcome_screen") {
                    WelcomeScreen()
                }
            }

            authViewModel.isLoginSuccess.observe(this) { isSuccess ->
                if (isSuccess) {
                    navController.navigate("welcome_screen") // Navegar a la pantalla de bienvenida
                }
            }
        }
    }
}
