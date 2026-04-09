package com.vishal.manodost.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.vishal.manodost.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling authentication and user data
 */
class AuthRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("ManoDostPrefs", Context.MODE_PRIVATE)
    private val apiService = RetrofitClient.apiService
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_LANGUAGE = "user_language"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    // ==================== LOCAL STORAGE ====================
    
    fun saveUserSession(userId: String, accessToken: String, name: String, language: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_LANGUAGE, language)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    
    fun getUserLanguage(): String = prefs.getString(KEY_USER_LANGUAGE, "en") ?: "en"
    
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    fun clearSession() {
        prefs.edit().clear().apply()
    }
    
    fun saveLanguagePreference(language: String) {
        prefs.edit().putString(KEY_USER_LANGUAGE, language).apply()
    }
    
    // ==================== API CALLS ====================
    
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String? = null,
        parentPhone: String? = null,
        isJunior: Boolean = false,
        language: String = "en"
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SignUpRequest(email, password, name, phone, parentPhone, isJunior, language)
            val response = apiService.signUp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()!!
                saveUserSession(
                    authResponse.user_id!!,
                    authResponse.session?.access_token ?: "",
                    name,
                    language
                )
                Result.success(authResponse)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Sign up failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SignInRequest(email, password)
            val response = apiService.signIn(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()!!
                saveUserSession(
                    authResponse.user_id!!,
                    authResponse.session?.access_token ?: "",
                    authResponse.profile?.name ?: "",
                    authResponse.profile?.preferred_language ?: "en"
                )
                Result.success(authResponse)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Sign in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.signOut()
            clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            clearSession() // Clear local session even if API call fails
            Result.success(Unit)
        }
    }
    
    suspend fun updateLanguage(language: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserId() ?: return@withContext Result.failure(Exception("User not logged in"))
            val request = LanguageRequest(language)
            val response = apiService.updateLanguage(userId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                saveLanguagePreference(language)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update language"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
