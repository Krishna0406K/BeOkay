package com.vishal.manodost.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service for Voice Agent
 */
interface VoiceApiService {
    
    @POST("voice/start")
    suspend fun startVoiceSession(@Body request: StartVoiceRequest): Response<StartVoiceResponse>
    
    @Multipart
    @POST("voice/transcribe")
    suspend fun transcribeAudio(
        @Part audio: MultipartBody.Part,
        @Part("session_id") sessionId: RequestBody
    ): Response<TranscribeResponse>
    
    @POST("voice/process")
    suspend fun processVoiceMessage(@Body request: ProcessVoiceRequest): Response<ProcessVoiceResponse>
    
    @POST("voice/end")
    suspend fun endVoiceSession(@Body request: EndVoiceRequest): Response<EndVoiceResponse>
    
    @GET("voice/health")
    suspend fun voiceHealthCheck(): Response<VoiceHealthResponse>
}

// ==================== REQUEST MODELS ====================

data class StartVoiceRequest(
    val language: String,
    val device_id: String
)

data class ProcessVoiceRequest(
    val session_id: String,
    val text: String
)

data class EndVoiceRequest(
    val session_id: String
)

// ==================== RESPONSE MODELS ====================

data class StartVoiceResponse(
    val success: Boolean,
    val session_id: String?,
    val user_id: String?,
    val message: String?,
    val greeting: String?,
    val error: String? = null
)

data class TranscribeResponse(
    val success: Boolean,
    val transcription: String?,
    val session_id: String?,
    val error: String? = null
)

data class ProcessVoiceResponse(
    val success: Boolean,
    val response: String?,
    val session_id: String?,
    val metadata: VoiceMetadata?,
    val error: String? = null
)

data class VoiceMetadata(
    val phq9_total: Int?,
    val gad7_total: Int?,
    val risk_level: String?,
    val emergency_contact: String?,
    val primary_emotion: String?,
    val items_assessed: List<String>?
)

data class EndVoiceResponse(
    val success: Boolean,
    val message: String?,
    val summary: VoiceSummary?,
    val farewell: String?,
    val error: String? = null
)

data class VoiceSummary(
    val session_id: String,
    val total_messages: Int,
    val phq9_score: Int?,
    val gad7_score: Int?,
    val risk_level: String?,
    val primary_emotion: String?,
    val emergency_contact: String?
)

data class VoiceHealthResponse(
    val status: String,
    val service: String,
    val version: String,
    val groq_api: String
)
