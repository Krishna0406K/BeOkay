package com.vishal.manodost.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vishal.manodost.ui.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameWebViewScreen(
    gameId: Int,
    onBackClick: () -> Unit
) {
    val game = com.vishal.manodost.data.model.GamesData.games.find { it.id == gameId }
        ?: com.vishal.manodost.data.model.GamesData.games.first()
    
    val isHindi = AppSettings.isHindi
    val darkGreen = Color(0xFF1E3A2D)
    var isLoading by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = game.name,
                        fontWeight = FontWeight.Bold,
                        color = darkGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = darkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        // Enable all necessary settings
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = false
                            setSupportZoom(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            databaseEnabled = true
                            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        }
                        
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?
                            ): Boolean {
                                return false
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }
                            
                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                isLoading = false
                            }
                        }
                        
                        webChromeClient = android.webkit.WebChromeClient()
                        
                        loadUrl(game.url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = darkGreen
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isHindi) "लोड हो रहा है..." else "Loading...",
                            color = darkGreen
                        )
                    }
                }
            }
        }
    }
}
