package com.vishal.manodost.ui

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.vishal.manodost.ml.EmotionAnalyzer
import com.vishal.manodost.ui.components.ChatBubble
import com.vishal.manodost.utils.rememberCameraPermission
import com.vishal.manodost.viewmodel.IChatViewModel
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    viewModel: IChatViewModel,
    onHomeClick: () -> Unit,
    onCircleClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermission = rememberCameraPermission()
    
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isHindi = AppSettings.isHindi
    val listState = rememberLazyListState()
    
    // Emotion detection
    val emotionAnalyzer = remember { EmotionAnalyzer(context) }
    var currentEmotion by remember { mutableStateOf<String?>(null) }
    var emotionConfidence by remember { mutableStateOf<Float?>(null) }
    var cameraStarted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
    
    // Request camera permission immediately on first launch
    LaunchedEffect(Unit) {
        println("[CHAT-SCREEN] Component mounted, checking camera permission...")
        if (!cameraPermission.hasPermission && !permissionRequested) {
            println("[CHAT-SCREEN] No permission, requesting now...")
            permissionRequested = true
            delay(300) // Small delay to ensure UI is ready
            cameraPermission.requestPermission()
        } else if (cameraPermission.hasPermission) {
            println("[CHAT-SCREEN] Camera permission already granted")
        }
    }
    
    // Start emotion detection when permission granted
    LaunchedEffect(cameraPermission.hasPermission) {
        if (cameraPermission.hasPermission && !cameraStarted) {
            println("[CHAT-SCREEN] Permission granted! Starting emotion detection in 500ms...")
            delay(500)
            cameraStarted = true
            println("[CHAT-SCREEN] Camera started flag set to true")
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            emotionAnalyzer.shutdown()
        }
    }

    val darkGreen = Color(0xFF385E4D)
    val lightBackground = Color(0xFFF2F7F4)
    val pureWhite = Color.White

    Box(modifier = Modifier.fillMaxSize()) {
        // Hidden camera preview for emotion detection - always create it
        if (cameraPermission.hasPermission && cameraStarted) {
            AndroidView(
                factory = { ctx ->
                    println("[CHAT-SCREEN] Creating PreviewView for camera...")
                    PreviewView(ctx).apply {
                        post {
                            println("[CHAT-SCREEN] PreviewView posted, starting analysis...")
                            emotionAnalyzer.startAnalysis(
                                lifecycleOwner = lifecycleOwner,
                                previewView = this,
                                onEmotionDetected = { result ->
                                    currentEmotion = result.emotion
                                    emotionConfidence = result.confidence
                                    println("[CHAT-SCREEN] ✅ Emotion updated: ${result.emotion} (${result.confidence})")
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.size(1.dp) // Hidden but active
            )
        } else {
            // Show status
            println("[CHAT-SCREEN] Camera not started - Permission: ${cameraPermission.hasPermission}, Started: $cameraStarted")
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = listOf(Color.White, lightBackground)))
                .imePadding()
        ) {
        // --- TOP APP BAR ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = darkGreen, modifier = Modifier.size(28.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ManoDost AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = darkGreen)
                // Show current emotion and camera status
                if (currentEmotion != null) {
                    Text(
                        text = "😊 $currentEmotion",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                } else if (cameraPermission.hasPermission) {
                    Text(
                        text = "📷 Analyzing...",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = "📷 Camera needed",
                        fontSize = 10.sp,
                        color = Color.Red
                    )
                }
            }
            Spacer(modifier = Modifier.size(28.dp))
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(color = Color(0xFFEBEFEF), shape = RoundedCornerShape(50), modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = if (isHindi) "आज • वेलनेस स्क्रीनिंग" else "TODAY • WELLNESS SCREENING",
                    fontSize = 10.sp, color = Color(0xFF4A5550),
                    letterSpacing = 1.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message.text, message.isUser)
            }
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = darkGreen, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        val options = viewModel.getCurrentOptions()
        if (!isLoading) {
            if (options.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                    options.forEachIndexed { index, option ->
                        Surface(
                            onClick = { viewModel.answerQuestion(index) },
                            shape = RoundedCornerShape(50),
                            color = pureWhite,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(option, fontSize = 14.sp, color = Color(0xFF1C1E21))
                                Box(modifier = Modifier.size(18.dp).background(Color(0xFFC9D1CD), CircleShape))
                            }
                        }
                    }
                }
            }

            var inputText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(50), color = pureWhite, shadowElevation = 1.dp, modifier = Modifier.weight(1f).height(48.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF1C1E21)),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text(if (isHindi) "अपना उत्तर टाइप करें..." else "Type your answer...", color = Color(0xFFA5B4AC), fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier.size(48.dp).background(darkGreen, CircleShape)
                        .clickable {
                            if (inputText.isNotBlank()) {
                                // Send message with emotion data
                                if (viewModel is com.vishal.manodost.viewmodel.ChatViewModelSimple) {
                                    viewModel.answerWithTextAndEmotion(inputText, currentEmotion, emotionConfidence)
                                } else {
                                    viewModel.answerWithText(inputText)
                                }
                                inputText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = pureWhite, modifier = Modifier.size(20.dp))
                }
            }
        }

        // --- UPDATED BOTTOM NAVIGATION BAR ---
        Surface(
            color = pureWhite,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 12.dp).navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onHomeClick() }) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF5E6D66))
                    Text(if (isHindi) "होम" else "Home", fontSize = 12.sp, color = Color(0xFF5E6D66), modifier = Modifier.padding(top = 4.dp))
                }

                // 2. Chat (Active)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(56.dp).background(Color(0xFF638C7A), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = pureWhite)
                    }
                    Text(if (isHindi) "चैट" else "Chat", fontSize = 12.sp, color = darkGreen, modifier = Modifier.padding(top = 4.dp), fontWeight = FontWeight.Bold)
                }

                // 3. The Circle (Replaces Insights)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onCircleClick() }) {
                    Icon(Icons.Default.Groups, contentDescription = "The Circle", tint = Color(0xFF5E6D66))
                    Text(if (isHindi) "सर्कल" else "Circle", fontSize = 12.sp, color = Color(0xFF5E6D66), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

}
