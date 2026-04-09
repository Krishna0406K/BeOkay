package com.vishal.manodost.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global state manager for the App's Visual Theme.
 * Using 'by mutableStateOf' ensures that the entire App
 * (starting from MainActivity) recomposes instantly when toggled.
 */
object ThemeSettings {
    var isDarkMode by mutableStateOf(false)
}