package com.adrifdezz.lostandfound.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.adrifdezz.lostandfound.data.AuthRepository
import com.adrifdezz.lostandfound.ui.viewmodel.AuthViewModel
import com.adrifdezz.lostandfound.ui.components.AuthScreen

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModel.Factory(AuthRepository())
        )[AuthViewModel::class.java]

        setContent {
            AuthScreen(authViewModel)
        }
    }
}
