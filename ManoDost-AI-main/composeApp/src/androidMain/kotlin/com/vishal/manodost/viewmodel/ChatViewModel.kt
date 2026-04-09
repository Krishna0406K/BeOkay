package com.vishal.manodost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishal.manodost.data.model.Message
import com.vishal.manodost.data.model.Question
import com.vishal.manodost.data.repository.ChatRepository
import com.vishal.manodost.ui.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel(), IChatViewModel {

    // ✅ Full PHQ-2/GAD-2 style logic for the hackathon
    private val questions = listOf(
        Question(
            textEn = "How often have you felt little interest in doing things?",
            textHi = "कितनी बार आपको काम करने में कम दिलचस्पी महसूस हुई है?",
            optionsEn = listOf("Not at all", "Several days", "More than half", "Nearly every day"),
            optionsHi = listOf("बिल्कुल नहीं", "कुछ दिन", "आधे से अधिक दिन", "लगभग हर दिन")
        ),
        Question(
            textEn = "How often have you felt down or depressed?",
            textHi = "कितनी बार आपने उदास या निराश महसूस किया है?",
            optionsEn = listOf("Not at all", "Several days", "More than half", "Nearly every day"),
            optionsHi = listOf("बिल्कुल नहीं", "कुछ दिन", "आधे से अधिक दिन", "लगभग हर दिन")
        )
    )

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    override val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    // ✅ NOTE: Removed _finalResponse as we are skipping the Result Screen!

    private var currentQuestionIndex = 0
    private var score = 0

    override fun startChat() {
        currentQuestionIndex = 0
        score = 0
        _messages.value = emptyList()

        val firstQuestion = questions[0]
        val textToShow = if (AppSettings.isHindi) firstQuestion.textHi else firstQuestion.textEn
        _messages.value += Message(textToShow, false)
    }

    override fun answerQuestion(answerIndex: Int) {
        if (currentQuestionIndex >= questions.size) return

        val currentQ = questions[currentQuestionIndex]
        val selectedAnswer = if (AppSettings.isHindi) currentQ.optionsHi[answerIndex] else currentQ.optionsEn[answerIndex]

        _messages.value += Message(selectedAnswer, true)

        score += answerIndex
        currentQuestionIndex++

        if (currentQuestionIndex < questions.size) {
            val nextQuestion = questions[currentQuestionIndex]
            val nextText = if (AppSettings.isHindi) nextQuestion.textHi else nextQuestion.textEn
            _messages.value += Message(nextText, false)
        } else {
            fetchAiResult()
        }
    }

    override fun answerWithText(typedText: String) {
        _messages.value += Message(typedText, true)
        score += 1
        currentQuestionIndex++

        if (currentQuestionIndex < questions.size) {
            val nextQuestion = questions[currentQuestionIndex]
            val nextText = if (AppSettings.isHindi) nextQuestion.textHi else nextQuestion.textEn
            _messages.value += Message(nextText, false)
        } else {
            fetchAiResult()
        }
    }

    override fun getCurrentOptions(): List<String> {
        if (currentQuestionIndex >= questions.size || _isLoading.value) return emptyList()
        val currentQ = questions[currentQuestionIndex]
        return if (AppSettings.isHindi) currentQ.optionsHi else currentQ.optionsEn
    }

    override fun sendMessage(text: String) {
        // For compatibility with IChatViewModel interface
        answerWithText(text)
    }

    private fun fetchAiResult() {
        viewModelScope.launch {
            _isLoading.value = true

            val loadingMsg = if (AppSettings.isHindi)
                "आपके लिए कुछ सुझाव तैयार कर रहा हूँ..."
            else
                "Preparing some suggestions for you..."

            _messages.value += Message(loadingMsg, false)

            try {
                // ✅ We call the repository but display the result right here in the chat
                val response = repository.getAiAnalysis(score)

                // Add the AI's personalized suggestion as a final message
                val finalMessage = response.suggestion // Assuming 'suggestion' is a string from your AI
                _messages.value += Message(finalMessage, false)

                // Optional: Add a friendly follow-up
                val followUp = if (AppSettings.isHindi)
                    "आप नीचे दिए गए 'सर्कल' बटन पर जाकर दूसरों से भी बात कर सकते हैं।"
                else
                    "You can also talk to others by clicking the 'Circle' button below."

                _messages.value += Message(followUp, false)

            } catch (e: Exception) {
                val errorMsg = if (AppSettings.isHindi)
                    "क्षमा करें, मुझे अभी सुझाव देने में समस्या हो रही है।"
                else
                    "Sorry, I'm having trouble giving suggestions right now."
                _messages.value += Message(errorMsg, false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}