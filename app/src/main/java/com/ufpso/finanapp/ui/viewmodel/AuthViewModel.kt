package com.ufpso.finanapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ufpso.finanapp.data.local.database.AppDatabase
import com.ufpso.finanapp.data.local.entity.UserEntity
import com.ufpso.finanapp.data.repository.AuthResult
import com.ufpso.finanapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(
        AppDatabase.getInstance(application).userDao()
    )

    var loginError by mutableStateOf<String?>(null)
        private set
    var registerError by mutableStateOf<String?>(null)
        private set

    // Usuario con sesión iniciada (null = ninguno)
    var currentUser by mutableStateOf<UserEntity?>(null)
        private set

    fun register(
        fullName: String,
        email: String,
        username: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repository.register(fullName, email, username, password)) {
                is AuthResult.Success -> {
                    registerError = null
                    onSuccess()
                }
                is AuthResult.Error -> registerError = result.message
            }
        }
    }

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val result = repository.login(username, password)) {
                is AuthResult.Success -> {
                    loginError = null
                    currentUser = result.user // se guarda ANTES de navegar
                    onSuccess()
                }
                is AuthResult.Error -> loginError = result.message
            }
        }
    }

    fun logout() {
        currentUser = null
    }

    fun clearErrors() {
        loginError = null
        registerError = null
    }
}