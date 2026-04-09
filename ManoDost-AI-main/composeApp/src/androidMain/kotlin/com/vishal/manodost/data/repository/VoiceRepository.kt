package com.vishal.manodost.data.repository

import android.content.Context
import android.provider.Settings
import com.vishal.manodost.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Repository for Voice Agent interactions
 */
class VoiceRepository(private val context: Context) {
    
    private val voiceApiService = RetrofitClient.voiceApiService
    private val prefs = context.getSharedPreferences("manodost_voice_prefs", Context.MODE_PRIVATE)
    
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
    
    suspend fun startVoiceSession(language: String = "en"): Result<StartVoiceResponse> = withContext(Dispatchers.IO) {
        try {
            val request = StartVoiceRequest(
                language = language,
                device_id = getDeviceId()
            )
            val response = voiceApiService.startVoiceSession(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                prefs.edit()
                    .putString("voice_session_id", body.session_id)
                    .putString("voice_user_id", body.user_id)
                    .putString("voice_language", language)
                    .apply()
                Result.success(body)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to start voice session"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun transcribeAudio(audioFile: File, sessionId: String): Result<TranscribeResponse> = withContext(Dispatchers.IO) {
        try {
            // Create multipart request
            val requestFile = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)
            val sessionIdBody = sessionId.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = voiceApiService.transcribeAudio(audioPart, sessionIdBody)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to transcribe audio"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun processVoiceMessage(text: String, sessionId: String): Result<ProcessVoiceResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ProcessVoiceRequest(
                session_id = sessionId,
                text = text
            )
            val response = voiceApiService.processVoiceMessage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to process message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun endVoiceSession(sessionId: String): Result<EndVoiceResponse> = withContext(Dispatchers.IO) {
        try {
            val request = EndVoiceRequest(session_id = sessionId)
            val response = voiceApiService.endVoiceSession(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Clear session info
                prefs.edit()
                    .remove("voice_session_id")
                    .remove("voice_user_id")
                    .apply()
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to end voice session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkVoiceHealth(): Result<VoiceHealthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = voiceApiService.voiceHealthCheck()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Voice service not available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentLanguage(): String {
        return prefs.getString("voice_language", "en") ?: "en"
    }
    
    fun getVoiceSessionId(): String? {
        return prefs.getString("voice_session_id", null)
    }
    
    fun clearVoiceSession() {
        prefs.edit()
            .remove("voice_session_id")
            .remove("voice_user_id")
            .apply()
    }
}
