package com.adrifdezz.lostandfound.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }

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
        TextButton(onClick = { isLogin = !isLogin }) {
            Text(if (isLogin) "¿Necesitas una cuenta? Registrate" else "¿Tienes ya una cuenta? Inicia Sesion")
        }
    }
}
