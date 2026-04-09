package com.vishal.manodost.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vishal.manodost.viewmodel.VoiceViewModel
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    onEndCall: () -> Unit,
    voiceViewModel: VoiceViewModel
) {

    // Exact colors from your design
    val darkGreen = Color(0xFF1E3A2D)
    val lightBackground = Color(0xFFF4F9F6)
    val coralRed = Color(0xFFF08A74)
    val coralRedText = Color(0xFF9E4030)
    val lightGray = Color(0xFFEBECEB)
    val mintGreen = Color(0xFFAEE1C1)
    val textGray = Color(0xFF8B9D94)
    
    val context = LocalContext.current
    val isHindi = AppSettings.isHindi
    
    // Voice state
    val isCallActive by voiceViewModel.isCallActive.collectAsState()
    val isProcessing by voiceViewModel.isProcessing.collectAsState()
    val isRecording by voiceViewModel.isRecording.collectAsState()
    val isSpeaking by voiceViewModel.isSpeaking.collectAsState()
    val currentResponse by voiceViewModel.currentResponse.collectAsState()
    val greeting by voiceViewModel.greeting.collectAsState()
    val errorMessage by voiceViewModel.errorMessage.collectAsState()
    val sessionSummary by voiceViewModel.sessionSummary.collectAsState()
    val farewell by voiceViewModel.farewell.collectAsState()
    
    // UI state
    var callDuration by remember { mutableStateOf(0) }
    var hasPermission by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    var isFirstMessage by remember { mutableStateOf(true) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            // Start voice call after permission granted
            val language = if (isHindi) "hi" else "en"
            voiceViewModel.startVoiceCall(language)
        } else {
            // Permission denied - show error or exit
            onEndCall()
        }
    }
    
    // Check permission on start
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.RECORD_AUDIO
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            if (!isCallActive) {
                val language = if (isHindi) "hi" else "en"
                voiceViewModel.startVoiceCall(language)
            }
        } else {
            permissionLauncher.launch(permission)
        }
    }
    
    // Speak greeting once when call starts
    LaunchedEffect(greeting) {
        if (greeting != null && isFirstMessage) {
            isFirstMessage = false
        }
    }
    
    // Show summary when call ends
    LaunchedEffect(isCallActive) {
        if (!isCallActive && sessionSummary != null) {
            showSummary = true
        }
    }
    
    // Timer for call duration
    LaunchedEffect(isCallActive) {
        if (isCallActive) {
            while (isCallActive) {
                delay(1000)
                callDuration++
            }
        }
    }
    
    // Format call duration
    val minutes = callDuration / 60
    val seconds = callDuration % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, lightBackground)
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // --- 1. HEADER ---
        Text(
            text = "BeOkay",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1E21)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        if (isCallActive) Color(0xFF84C59D) else Color.Gray,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isHindi) "वॉयस कॉल" else "Voice Call",
                fontSize = 16.sp,
                color = textGray
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // --- 2. CENTER AVATAR (Blurred Leaf Effect) ---
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isRecording) {
                            listOf(
                                Color(0xFFFF6B6B), // Red when recording
                                Color(0xFFD63031)
                            )
                        } else if (isSpeaking) {
                            listOf(
                                Color(0xFF74B9FF), // Blue when speaking
                                Color(0xFF0984E3)
                            )
                        } else {
                            listOf(
                                Color(0xFF6B9B7B), // Lighter green center
                                Color(0xFF2E5940)  // Darker green edge
                            )
                        }
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(80.dp)
                )
            } else if (isRecording) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Recording",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            } else if (isSpeaking) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Speaking",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "ManoDost AI Voice",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Show greeting or current response
        if (!isCallActive && greeting != null) {
            Text(
                text = greeting ?: "",
                fontSize = 16.sp,
                color = darkGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        } else if (currentResponse != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = currentResponse ?: "",
                    fontSize = 14.sp,
                    color = darkGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Show error if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                fontSize = 12.sp,
                color = coralRed,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // --- 3. TIMER ---
        Text(
            text = timeString,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            color = darkGreen,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. CONTROL PANEL OR SUMMARY ---
        if (showSummary && sessionSummary != null) {
            // Show Summary
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = if (isHindi) "सत्र सारांश" else "Session Summary",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkGreen
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    farewell?.let { farewellText ->
                        Text(
                            text = farewellText,
                            fontSize = 14.sp,
                            color = textGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Suggestions based on conversation
                    Text(
                        text = if (isHindi) "सुझाव:" else "Suggestions:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = darkGreen
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    sessionSummary?.let { summary ->
                        val suggestions = when {
                            summary.risk_level == "High" -> if (isHindi) {
                                listOf(
                                    "कृपया तुरंत किसी मानसिक स्वास्थ्य पेशेवर से संपर्क करें",
                                    "अपने परिवार या दोस्तों से बात करें",
                                    "आपातकालीन हेल्पलाइन पर कॉल करें"
                                )
                            } else {
                                listOf(
                                    "Please contact a mental health professional immediately",
                                    "Talk to your family or friends",
                                    "Call emergency helpline if needed"
                                )
                            }
                            summary.risk_level == "Mid" -> if (isHindi) {
                                listOf(
                                    "नियमित व्यायाम और योग करें",
                                    "अच्छी नींद लें (7-8 घंटे)",
                                    "किसी काउंसलर से बात करने पर विचार करें"
                                )
                            } else {
                                listOf(
                                    "Practice regular exercise and yoga",
                                    "Get good sleep (7-8 hours)",
                                    "Consider talking to a counselor"
                                )
                            }
                            else -> if (isHindi) {
                                listOf(
                                    "अपनी दिनचर्या बनाए रखें",
                                    "दोस्तों और परिवार के साथ समय बिताएं",
                                    "अपनी पसंद की चीजें करें"
                                )
                            } else {
                                listOf(
                                    "Maintain your daily routine",
                                    "Spend time with friends and family",
                                    "Do things you enjoy"
                                )
                            }
                        }
                        
                        suggestions.forEachIndexed { index, suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${index + 1}. ",
                                    fontSize = 14.sp,
                                    color = darkGreen
                                )
                                Text(
                                    text = suggestion,
                                    fontSize = 14.sp,
                                    color = darkGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Close button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(mintGreen)
                            .clickable { 
                                voiceViewModel.resetSession()
                                onEndCall()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHindi) "बंद करें" else "Close",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkGreen
                        )
                    }
                }
            }
        } else {
            // Show Control Panel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFFF9F9F9),
                shadowElevation = 0.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFD6DFDA), RoundedCornerShape(50))
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // MIC BUTTON (Push to talk)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isRecording) coralRed 
                                        else if (isProcessing || isSpeaking) lightGray
                                        else mintGreen
                                    )
                                    .clickable(enabled = !isProcessing && !isSpeaking) {
                                        if (isRecording) {
                                            voiceViewModel.stopRecording()
                                        } else {
                                            voiceViewModel.startRecording()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        color = darkGreen,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                                        tint = if (isRecording) Color.White else darkGreen,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (isRecording) {
                                    if (isHindi) "रिकॉर्डिंग..." else "Recording..."
                                } else if (isProcessing) {
                                    if (isHindi) "प्रोसेसिंग..." else "Processing..."
                                } else if (isSpeaking) {
                                    if (isHindi) "बोल रहा है..." else "Speaking..."
                                } else {
                                    if (isHindi) "बोलने के लिए टैप करें" else "Tap to Speak"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRecording) coralRedText else darkGreen,
                                textAlign = TextAlign.Center
                            )
                        }

                        // END CALL BUTTON
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(coralRed)
                                    .clickable {
                                        if (isCallActive) {
                                            voiceViewModel.endVoiceCall()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CallEnd,
                                    contentDescription = "End Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (isHindi) "कॉल समाप्त" else "End Call",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = coralRedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. FOOTER DISCLAIMER ---
        Text(
            text = if (isHindi) "आप एक सुरक्षित, निजी स्थान में हैं। आपकी बातचीत\nएन्क्रिप्टेड है।" else "You are in a safe, private space. Your conversation is\nencrypted.",
            fontSize = 12.sp,
            color = Color(0xFFA5B4AC),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}