package com.vishal.manodost.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishal.manodost.data.model.Message
import com.vishal.manodost.data.repository.ChatRepositorySimple
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModelSimple(context: Context) : ViewModel(), IChatViewModel {

    private val repository = ChatRepositorySimple(context)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    override val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage

    private var sessionStarted = false

    init {
        _currentLanguage.value = repository.getCurrentLanguage()
        // Sync AppSettings with repository language
        com.vishal.manodost.ui.AppSettings.setLanguage(_currentLanguage.value)
        
        // Check if we already have a session
        val existingSessionId = repository.getSessionId()
        if (existingSessionId != null) {
            sessionStarted = true
            // Add welcome back message
            val welcomeMsg = if (_currentLanguage.value == "hi")
                "नमस्ते! मैं वापस आ गया हूँ। आप मुझसे कुछ भी बात कर सकते हैं। 😊"
            else
                "Hello! I'm back. You can talk to me about anything. 😊"
            _messages.value += Message(welcomeMsg, false)
        }
    }

    override fun startChat() {
        // If session already exists, just show welcome message
        if (sessionStarted) {
            if (_messages.value.isEmpty()) {
                val welcomeMsg = if (_currentLanguage.value == "hi")
                    "नमस्ते! मैं ManoDost हूँ। आप मुझसे कुछ भी बात कर सकते हैं। 😊"
                else
                    "Hello! I'm ManoDost. You can talk to me about anything. 😊"
                _messages.value += Message(welcomeMsg, false)
            }
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.startSession(_currentLanguage.value)
                if (result.isSuccess) {
                    sessionStarted = true
                    // Add welcome message
                    val welcomeMsg = if (_currentLanguage.value == "hi")
                        "नमस्ते! मैं ManoDost हूँ। आप मुझसे कुछ भी बात कर सकते हैं। 😊"
                    else
                        "Hello! I'm ManoDost. You can talk to me about anything. 😊"
                    _messages.value += Message(welcomeMsg, false)
                } else {
                    val error = result.exceptionOrNull()
                    val errorMsg = if (_currentLanguage.value == "hi")
                        "कनेक्शन में समस्या: ${error?.message ?: "अज्ञात त्रुटि"}"
                    else
                        "Connection issue: ${error?.message ?: "Unknown error"}"
                    _messages.value += Message(errorMsg, false)
                    
                    // Log error for debugging
                    println("StartChat Error: ${error?.message}")
                    error?.printStackTrace()
                }
            } catch (e: Exception) {
                val errorMsg = if (_currentLanguage.value == "hi")
                    "त्रुटि: ${e.message}"
                else
                    "Error: ${e.message}"
                _messages.value += Message(errorMsg, false)
                println("StartChat Exception: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        _messages.value += Message(text, true)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // If no session, try to start one first
                if (!sessionStarted || repository.getSessionId() == null) {
                    val startResult = repository.startSession(_currentLanguage.value)
                    if (startResult.isSuccess) {
                        sessionStarted = true
                    } else {
                        val errorMsg = if (_currentLanguage.value == "hi")
                            "सत्र शुरू करने में विफल। कृपया पुनः प्रयास करें।"
                        else
                            "Failed to start session. Please try again."
                        _messages.value += Message(errorMsg, false)
                        _isLoading.value = false
                        return@launch
                    }
                }
                
                val result = repository.sendMessage(text)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null) {
                        // Add AI response
                        _messages.value += Message(response.response, false)
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val errorMsg = if (_currentLanguage.value == "hi")
                        "जवाब देने में समस्या: ${error?.message ?: "अज्ञात त्रुटि"}"
                    else
                        "Trouble responding: ${error?.message ?: "Unknown error"}"
                    _messages.value += Message(errorMsg, false)
                    
                    println("SendMessage Error: ${error?.message}")
                    error?.printStackTrace()
                }
            } catch (e: Exception) {
                val errorMsg = if (_currentLanguage.value == "hi")
                    "त्रुटि: ${e.message}"
                else
                    "Error: ${e.message}"
                _messages.value += Message(errorMsg, false)
                println("SendMessage Exception: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun answerWithTextAndEmotion(typedText: String, emotion: String?, emotionConfidence: Float?) {
        sendMessageWithEmotion(typedText, emotion, emotionConfidence)
    }
    
    private fun sendMessageWithEmotion(text: String, emotion: String?, emotionConfidence: Float?) {
        if (text.isBlank()) return

        // Add user message
        _messages.value += Message(text, true)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // If no session, try to start one first
                if (!sessionStarted || repository.getSessionId() == null) {
                    val startResult = repository.startSession(_currentLanguage.value)
                    if (startResult.isSuccess) {
                        sessionStarted = true
                    } else {
                        val errorMsg = if (_currentLanguage.value == "hi")
                            "सत्र शुरू करने में विफल। कृपया पुनः प्रयास करें।"
                        else
                            "Failed to start session. Please try again."
                        _messages.value += Message(errorMsg, false)
                        _isLoading.value = false
                        return@launch
                    }
                }
                
                val result = repository.sendMessageWithEmotion(text, emotion, emotionConfidence)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null) {
                        // Add AI response
                        _messages.value += Message(response.response, false)
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val errorMsg = if (_currentLanguage.value == "hi")
                        "जवाब देने में समस्या: ${error?.message ?: "अज्ञात त्रुटि"}"
                    else
                        "Trouble responding: ${error?.message ?: "Unknown error"}"
                    _messages.value += Message(errorMsg, false)
                    
                    println("SendMessage Error: ${error?.message}")
                    error?.printStackTrace()
                }
            } catch (e: Exception) {
                val errorMsg = if (_currentLanguage.value == "hi")
                    "त्रुटि: ${e.message}"
                else
                    "Error: ${e.message}"
                _messages.value += Message(errorMsg, false)
                println("SendMessage Exception: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLanguage() {
        val newLanguage = if (_currentLanguage.value == "en") "hi" else "en"
        updateLanguage(newLanguage)
    }

    fun updateLanguage(newLanguage: String) {
        _currentLanguage.value = newLanguage
        // Sync AppSettings
        com.vishal.manodost.ui.AppSettings.setLanguage(newLanguage)

        viewModelScope.launch {
            try {
                repository.updateLanguage(newLanguage)
                val msg = if (newLanguage == "hi")
                    "भाषा हिंदी में बदल गई है। 🇮🇳"
                else
                    "Language changed to English. 🇬🇧"
                _messages.value += Message(msg, false)
            } catch (e: Exception) {
                // Ignore error, language still changed locally
            }
        }
    }

    override fun getCurrentOptions(): List<String> {
        // No options needed for free-form chat
        return emptyList()
    }

    override fun answerQuestion(answerIndex: Int) {
        // Not used in free-form chat
    }

    override fun answerWithText(typedText: String) {
        sendMessage(typedText)
    }
}
