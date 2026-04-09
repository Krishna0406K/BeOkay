package com.vishal.manodost.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service for ManoDost AI Backend
 */
interface ApiService {
    
    // ==================== AUTHENTICATION ====================
    
    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>
    
    @POST("api/auth/signin")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponse>
    
    @POST("api/auth/signout")
    suspend fun signOut(): Response<BasicResponse>
    
    // ==================== USER PROFILE ====================
    
    @GET("api/user/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<UserProfileResponse>
    
    @PUT("api/user/{userId}/language")
    suspend fun updateLanguage(
        @Path("userId") userId: String,
        @Body request: LanguageRequest
    ): Response<BasicResponse>
    
    // ==================== CHAT ====================
    
    @POST("api/chat/start")
    suspend fun startChat(@Body request: StartChatRequest): Response<StartChatResponse>
    
    @POST("api/chat/message")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageResponse>
    
    @GET("api/chat/session/{sessionId}")
    suspend fun getSession(@Path("sessionId") sessionId: Int): Response<SessionDetailsResponse>
    
    @POST("api/chat/session/{sessionId}/end")
    suspend fun endSession(@Path("sessionId") sessionId: Int): Response<BasicResponse>
    
    @GET("api/chat/history/{userId}")
    suspend fun getChatHistory(@Path("userId") userId: String): Response<ChatHistoryResponse>
    
    // ==================== HEALTH CHECK ====================
    
    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}

// ==================== REQUEST MODELS ====================

data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String? = null,
    val parent_phone: String? = null,
    val is_junior: Boolean = false,
    val language: String = "en"
)

data class SignInRequest(
    val email: String,
    val password: String
)

data class LanguageRequest(
    val language: String
)

data class StartChatRequest(
    val user_id: String,
    val language: String
)

data class SendMessageRequest(
    val session_id: Int,
    val message: String
)

// ==================== RESPONSE MODELS ====================

data class BasicResponse(
    val success: Boolean,
    val error: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val user_id: String? = null,
    val profile: UserProfile? = null,
    val session: Session? = null,
    val error: String? = null
)

data class UserProfile(
    val id: String,
    val user_id: String,
    val name: String,
    val phone: String?,
    val parent_phone: String?,
    val is_junior: Boolean,
    val preferred_language: String,
    val created_at: String
)

data class Session(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int
)

data class UserProfileResponse(
    val success: Boolean,
    val profile: UserProfile? = null,
    val error: String? = null
)

data class StartChatResponse(
    val success: Boolean,
    val session_id: Int? = null,
    val message: String? = null,
    val error: String? = null
)

data class MessageResponse(
    val success: Boolean,
    val response: String? = null,
    val metadata: MessageMetadata? = null,
    val error: String? = null
)

data class MessageMetadata(
    val phq9_total: Int?,
    val gad7_total: Int?,
    val risk_level: String?,
    val emergency_contact: String?,
    val primary_emotion: String?
)

data class SessionDetailsResponse(
    val session: ChatSession?,
    val message_count: Int,
    val messages: List<ChatMessage>,
    val emergency_contacts: List<EmergencyContact>
)

data class ChatSession(
    val id: Int,
    val user_id: String,
    val language: String,
    val created_at: String,
    val ended_at: String?,
    val is_active: Boolean,
    val current_phq9_score: Int,
    val current_gad7_score: Int,
    val max_phq9_score: Int,
    val max_gad7_score: Int,
    val risk_level: String
)

data class ChatMessage(
    val id: Int,
    val session_id: Int,
    val user_message: String,
    val ai_response: String,
    val phq9_score: Int?,
    val gad7_score: Int?,
    val risk_level: String?,
    val primary_emotion: String?,
    val timestamp: String
)

data class EmergencyContact(
    val id: Int,
    val session_id: Int,
    val contact: String,
    val timestamp: String
)

data class ChatHistoryResponse(
    val success: Boolean,
    val sessions: List<ChatSession>
)

data class HealthResponse(
    val status: String,
    val service: String,
    val version: String
)
