package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import androidx.navigation.NavHostController

@Composable
fun AuthScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val user by authViewModel.user.observeAsState()
    val loginError by authViewModel.error.observeAsState()

    LaunchedEffect(loginError) {
        if (loginError != null) {
            errorMessage = loginError ?: ""
        }
    }

    LaunchedEffect(user) {
        if (user != null && isLogin) {
            navController.navigate("welcome_screen")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electronico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = {
            if (isLogin) {
                authViewModel.login(email, password)
            } else {
                authViewModel.register(email, password)
            }
        }) {
            Text(if (isLogin) "Iniciar sesion" else "Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            isLogin = !isLogin
            email = ""
            password = ""
            errorMessage = ""
        }) {
            Text(if (isLogin) "¿Necesitas una cuenta? Registrate" else "¿Ya tienes cuenta? Inicia Sesión")
        }
    }
}