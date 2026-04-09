package com.vishal.manodost.viewmodel

import com.vishal.manodost.data.model.Message
import kotlinx.coroutines.flow.StateFlow

interface IChatViewModel {
    val messages: StateFlow<List<Message>>
    val isLoading: StateFlow<Boolean>
    
    fun startChat()
    fun sendMessage(text: String)
    fun getCurrentOptions(): List<String>
    fun answerQuestion(answerIndex: Int)
    fun answerWithText(typedText: String)
}
