package com.vishal.manodost.data.repository

import android.content.Context
import android.provider.Settings
import com.vishal.manodost.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Simplified Chat Repository - No Authentication Required
 */
class ChatRepositorySimple(private val context: Context) {
    
    private val apiService = RetrofitClient.apiServiceSimple
    private val prefs = context.getSharedPreferences("manodost_prefs", Context.MODE_PRIVATE)
    
    private fun getDeviceId(): String {
        var deviceId = prefs.getString("device_id", null)
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        return deviceId
    }
    
    suspend fun startSession(language: String = "en"): Result<StartSessionResponse> = withContext(Dispatchers.IO) {
        try {
            val request = StartSessionRequest(
                language = language,
                device_id = getDeviceId()
            )
            val response = apiService.startSession(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                // Save session info
                prefs.edit()
                    .putString("session_id", body.session_id)
                    .putString("user_id", body.user_id)
                    .putString("language", language)
                    .apply()
                Result.success(body)
            } else {
                Result.failure(Exception("Failed to start session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(message: String): Result<ChatResponse> = withContext(Dispatchers.IO) {
        try {
            val sessionId = prefs.getString("session_id", null)
                ?: return@withContext Result.failure(Exception("No active session"))
            
            val language = prefs.getString("language", "en") ?: "en"
            
            val request = ChatRequest(
                session_id = sessionId,
                message = message,
                language = language
            )
            val response = apiService.sendMessage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessageWithEmotion(message: String, emotion: String?, emotionConfidence: Float?): Result<ChatResponse> = withContext(Dispatchers.IO) {
        try {
            val sessionId = prefs.getString("session_id", null)
                ?: return@withContext Result.failure(Exception("No active session"))
            
            val language = prefs.getString("language", "en") ?: "en"
            
            val request = ChatRequest(
                session_id = sessionId,
                message = message,
                language = language,
                emotion = emotion,
                emotion_confidence = emotionConfidence
            )
            val response = apiService.sendMessage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateLanguage(language: String): Result<LanguageUpdateResponse> = withContext(Dispatchers.IO) {
        try {
            val sessionId = prefs.getString("session_id", null)
                ?: return@withContext Result.failure(Exception("No active session"))
            
            val request = LanguageUpdateRequest(
                session_id = sessionId,
                language = language
            )
            val response = apiService.updateLanguage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Update local language preference
                prefs.edit().putString("language", language).apply()
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update language"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentLanguage(): String {
        return prefs.getString("language", "en") ?: "en"
    }
    
    fun getSessionId(): String? {
        return prefs.getString("session_id", null)
    }
    
    fun clearSession() {
        prefs.edit()
            .remove("session_id")
            .remove("user_id")
            .apply()
    }
}
