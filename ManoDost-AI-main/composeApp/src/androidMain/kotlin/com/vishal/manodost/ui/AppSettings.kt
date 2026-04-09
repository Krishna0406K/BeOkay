package com.vishal.manodost.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// This object holds global state for your app.
// Because we use 'mutableStateOf', changing this will instantly redraw your screens!
object AppSettings {
    var isHindi by mutableStateOf(false)
    
    // Sync language with backend preference
    fun setLanguage(language: String) {
        isHindi = (language == "hi")
    }
    
    fun getLanguageCode(): String {
        return if (isHindi) "hi" else "en"
    }
}