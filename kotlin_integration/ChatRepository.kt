package com.vishal.manodost.repository

import com.vishal.manodost.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling chat operations
 */
class ChatRepository {
    
    private val apiService = RetrofitClient.apiService
    
    // ==================== CHAT SESSION ====================
    
    suspend fun startChatSession(userId: String, language: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = StartChatRequest(userId, language)
            val response = apiService.startChat(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val sessionId = response.body()!!.session_id!!
                Result.success(sessionId)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to start chat"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(sessionId: Int, message: String): Result<MessageResponse> = withContext(Dispatchers.IO) {
        try {
            val request = SendMessageRequest(sessionId, message)
            val response = apiService.sendMessage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSessionDetails(sessionId: Int): Result<SessionDetailsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSession(sessionId)
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get session details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun endSession(sessionId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.endSession(sessionId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to end session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getChatHistory(userId: String): Result<List<ChatSession>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getChatHistory(userId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.sessions)
            } else {
                Result.failure(Exception("Failed to get chat history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
