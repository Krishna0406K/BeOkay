package com.vishal.manodost.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vishal.manodost.network.AuthResponse
import com.vishal.manodost.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application.applicationContext)
    
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
    
    fun getUserId(): String? = authRepository.getUserId()
    
    fun getUserName(): String? = authRepository.getUserName()
    
    fun getUserLanguage(): String = authRepository.getUserLanguage()
    
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String? = null,
        parentPhone: String? = null,
        isJunior: Boolean = false,
        language: String = "en"
    ): Result<AuthResponse> {
        return authRepository.signUp(email, password, name, phone, parentPhone, isJunior, language)
    }
    
    suspend fun signIn(email: String, password: String): Result<AuthResponse> {
        return authRepository.signIn(email, password)
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
    
    suspend fun updateLanguage(language: String): Result<Unit> {
        return authRepository.updateLanguage(language)
    }
}
