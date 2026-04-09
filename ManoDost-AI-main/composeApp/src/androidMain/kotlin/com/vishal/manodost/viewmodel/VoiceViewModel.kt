package com.vishal.manodost.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishal.manodost.data.api.VoiceSummary
import com.vishal.manodost.data.repository.VoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

/**
 * ViewModel for Voice Agent interactions with TTS and Audio Recording
 */
class VoiceViewModel(private val context: Context) : ViewModel() {
    
    private val repository = VoiceRepository(context)
    
    // Text-to-Speech
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    
    // Audio Recording
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking
    
    private val _currentResponse = MutableStateFlow<String?>(null)
    val currentResponse: StateFlow<String?> = _currentResponse
    
    private val _greeting = MutableStateFlow<String?>(null)
    val greeting: StateFlow<String?> = _greeting
    
    private val _farewell = MutableStateFlow<String?>(null)
    val farewell: StateFlow<String?> = _farewell
    
    private val _sessionSummary = MutableStateFlow<VoiceSummary?>(null)
    val sessionSummary: StateFlow<VoiceSummary?> = _sessionSummary
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage
    
    private var currentSessionId: String? = null
    
    init {
        _currentLanguage.value = repository.getCurrentLanguage()
        com.vishal.manodost.ui.AppSettings.setLanguage(_currentLanguage.value)
        initializeTTS()
    }
    
    // ==================== TTS FUNCTIONS ====================
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                setTTSLanguage(_currentLanguage.value)
                println("TTS initialized successfully")
            } else {
                println("TTS initialization failed")
                _errorMessage.value = "Voice output not available"
            }
        }
        
        // Set up utterance listener
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }
            
            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }
            
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                println("TTS error for utterance: $utteranceId")
            }
        })
    }
    
    private fun setTTSLanguage(language: String) {
        if (!isTtsInitialized) return
        
        val locale = if (language == "hi") {
            Locale("hi", "IN")  // Hindi
        } else {
            Locale.US  // English
        }
        
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            println("TTS language not supported: $language")
            _errorMessage.value = "Voice language not available"
        } else {
            println("TTS language set to: $language")
        }
    }
    
    private fun speak(text: String) {
        if (!isTtsInitialized) {
            println("TTS not initialized")
            return
        }
        
        // Stop any ongoing speech
        tts?.stop()
        
        // Speak the text
        val utteranceId = System.currentTimeMillis().toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
        
        println("Speaking: $text")
    }
    
    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }
    
    // ==================== AUDIO RECORDING FUNCTIONS ====================
    
    fun startRecording() {
        if (_isRecording.value) {
            println("Already recording")
            return
        }
        
        try {
            // Create audio file
            audioFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                
                prepare()
                start()
                
                _isRecording.value = true
                println("Recording started: ${audioFile?.absolutePath}")
            }
        } catch (e: Exception) {
            println("Error starting recording: ${e.message}")
            e.printStackTrace()
            _errorMessage.value = "Could not start recording"
            _isRecording.value = false
        }
    }
    
    fun stopRecording() {
        if (!_isRecording.value) {
            println("Not recording")
            return
        }
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            
            println("Recording stopped: ${audioFile?.absolutePath}")
            
            // Process the recorded audio
            audioFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    processAudioFile(file)
                } else {
                    _errorMessage.value = "Recording failed"
                }
            }
        } catch (e: Exception) {
            println("Error stopping recording: ${e.message}")
            e.printStackTrace()
            _errorMessage.value = "Could not stop recording"
            _isRecording.value = false
        }
    }
    
    // ==================== VOICE CALL FUNCTIONS ====================
    
    fun startVoiceCall(language: String = "en") {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            
            try {
                setTTSLanguage(language)
                val result = repository.startVoiceSession(language)
                
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    currentSessionId = response?.session_id
                    _isCallActive.value = true
                    _greeting.value = response?.greeting
                    _currentLanguage.value = language
                    
                    com.vishal.manodost.ui.AppSettings.setLanguage(language)
                    
                    response?.greeting?.let { greeting ->
                        delay(500)
                        speak(greeting)
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to start voice call"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    fun processAudioFile(audioFile: File) {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            
            if (currentSessionId == null) {
                _errorMessage.value = "Session expired. Please restart call."
                _isProcessing.value = false
                return@launch
            }
            
            try {
                val sessionId = currentSessionId!!
                
                // Step 1: Transcribe audio
                val transcribeResult = repository.transcribeAudio(audioFile, sessionId)
                
                if (transcribeResult.isSuccess) {
                    val transcription = transcribeResult.getOrNull()?.transcription
                    
                    if (!transcription.isNullOrBlank()) {
                        // Step 2: Process transcribed text
                        val processResult = repository.processVoiceMessage(transcription, sessionId)
                        
                        if (processResult.isSuccess) {
                            val response = processResult.getOrNull()
                            _currentResponse.value = response?.response
                            
                            // Speak the AI response
                            response?.response?.let { aiResponse ->
                                speak(aiResponse)
                            }
                        } else {
                            _errorMessage.value = processResult.exceptionOrNull()?.message ?: "Failed to process message"
                        }
                    } else {
                        _errorMessage.value = "Could not transcribe audio"
                    }
                } else {
                    _errorMessage.value = transcribeResult.exceptionOrNull()?.message ?: "Failed to transcribe audio"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    fun processTextInput(text: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            
            if (currentSessionId == null) {
                _errorMessage.value = "Session expired. Please restart call."
                _isProcessing.value = false
                return@launch
            }
            
            try {
                val result = repository.processVoiceMessage(text, currentSessionId!!)
                
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    _currentResponse.value = response?.response
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to process message"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    fun endVoiceCall() {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            
            stopSpeaking()
            if (_isRecording.value) {
                stopRecording()
            }
            
            if (currentSessionId == null) {
                _isCallActive.value = false
                _isProcessing.value = false
                return@launch
            }
            
            try {
                val result = repository.endVoiceSession(currentSessionId!!)
                
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    _isCallActive.value = false
                    _farewell.value = response?.farewell
                    _sessionSummary.value = response?.summary
                    _currentResponse.value = null
                    _greeting.value = null
                    
                    response?.farewell?.let { farewell ->
                        speak(farewell)
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to end voice call"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearResponse() {
        _currentResponse.value = null
    }
    
    fun resetSession() {
        _isCallActive.value = false
        _isProcessing.value = false
        _isRecording.value = false
        _isSpeaking.value = false
        _currentResponse.value = null
        _greeting.value = null
        _farewell.value = null
        _sessionSummary.value = null
        _errorMessage.value = null
        currentSessionId = null
        
        stopSpeaking()
        if (_isRecording.value) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        mediaRecorder = null
        repository.clearVoiceSession()
    }
    
    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        
        if (_isRecording.value) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        mediaRecorder = null
    }
}
