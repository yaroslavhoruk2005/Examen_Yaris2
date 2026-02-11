package com.example.examen_yaris.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen_yaris.data.AuthManager
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authManager = AuthManager()

    fun login(
        email: String,
        password: String,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = authManager.login(email, password)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { e ->
                    val errorMsg = when {
                        e.message?.contains("password") == true -> "Contraseña incorrecta"
                        e.message?.contains("user") == true -> "Usuario no encontrado"
                        e.message?.contains("network") == true -> "Error de conexion"
                        else -> "Error al iniciar sesión: ${e.message}"
                    }
                    onResult(false, errorMsg)
                }
            )
        }
    }

    fun logout() {
        authManager.logout()
    }

    fun isUserLoggedIn(): Boolean = authManager.isUserLoggedIn()

    fun getCurrentUserEmail(): String? = authManager.getCurrentUser()?.email
}