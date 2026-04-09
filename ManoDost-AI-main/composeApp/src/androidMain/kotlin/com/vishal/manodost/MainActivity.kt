package com.vishal.manodost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.vishal.manodost.data.api.FakeApiImpl
import com.vishal.manodost.data.repository.ChatRepository
import com.vishal.manodost.navigation.NavGraph
import com.vishal.manodost.ui.ThemeSettings // ✅ Import our new theme manager
import com.vishal.manodost.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the simplified ViewModel with real API
        val viewModel = com.vishal.manodost.viewmodel.ChatViewModelSimple(this)

        setContent {
            // ✅ 1. Observe the global theme state
            val isDarkMode = ThemeSettings.isDarkMode

            // ✅ 2. Define our Color Schemes
            val colorScheme = if (isDarkMode) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }

            // ✅ 3. Wrap the whole app in the Dynamic Theme
            MaterialTheme(colorScheme = colorScheme) {
                NavGraph(viewModel)
            }
        }
    }
}