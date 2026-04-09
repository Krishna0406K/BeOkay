package com.vishal.manodost.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * Simplified API Service - No Authentication Required
 */
interface ApiServiceSimple {
    
    @POST("api/start")
    suspend fun startSession(@Body request: StartSessionRequest): Response<StartSessionResponse>
    
    @POST("api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>
    
    @POST("api/language")
    suspend fun updateLanguage(@Body request: LanguageUpdateRequest): Response<LanguageUpdateResponse>
    
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}

// Request Models
data class StartSessionRequest(
    val language: String = "en",
    val device_id: String = ""
)

data class ChatRequest(
    val session_id: String,
    val message: String,
    val language: String = "en",
    val emotion: String? = null,
    val emotion_confidence: Float? = null
)

data class LanguageUpdateRequest(
    val session_id: String,
    val language: String
)

// Response Models
data class StartSessionResponse(
    val success: Boolean,
    val session_id: String,
    val user_id: String,
    val message: String
)

data class ChatResponse(
    val success: Boolean,
    val response: String,
    val metadata: ChatMetadata? = null
)

data class ChatMetadata(
    val phq9_total: Int?,
    val gad7_total: Int?,
    val risk_level: String?,
    val emergency_contact: String?,
    val primary_emotion: String?
)

data class LanguageUpdateResponse(
    val success: Boolean,
    val message: String
)

// Note: HealthResponse is defined in ApiService.kt to avoid duplication
