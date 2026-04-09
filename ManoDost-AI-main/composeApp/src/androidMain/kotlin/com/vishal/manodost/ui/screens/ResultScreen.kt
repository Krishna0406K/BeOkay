package com.vishal.manodost.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishal.manodost.data.model.AiResponse

@Composable
fun ResultScreen(response: AiResponse, onHomeClick: () -> Unit) {
    // ✅ Read global language state
    val isHindi = AppSettings.isHindi

    // Exact colors from your design
    val darkGreen = Color(0xFF4B7B62)
    val textDark = Color(0xFF1C1E21)
    val textGray = Color(0xFF65676B)
    val lightBg = Color(0xFFF8FAF9)
    val cardBg = Color(0xFFF1F5F3)
    val bluePillBg = Color(0xFFD6E4FF)
    val bluePillText = Color(0xFF3B5B8C)

    // Dynamic color and text based on risk level
    val isLowRisk = response.risk.lowercase() == "low"
    val riskColor = if (isLowRisk) darkGreen else Color(0xFFD97762)

    // ✅ Translated Subtitles and Risk Values
    val riskSubtitle = if (isLowRisk) {
        if (isHindi) "सुरक्षित और संतुलित" else "Safe & Balanced"
    } else {
        if (isHindi) "ध्यान देने की आवश्यकता है" else "Requires Attention"
    }

    val translatedRiskWord = if (isHindi) {
        when (response.risk.lowercase()) {
            "low" -> "कम"
            "medium" -> "मध्यम"
            else -> "उच्च"
        }
    } else {
        response.risk
    }

    val progressFraction = if (isLowRisk) 0.15f else if (response.risk.lowercase() == "medium") 0.5f else 0.85f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBg)
    ) {
        // --- 1. TOP APP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = darkGreen, modifier = Modifier.size(28.dp))
            Text("ManoDost AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = darkGreen)

            // ✅ Replaced the profile icon with a Spacer to keep title centered
            Spacer(modifier = Modifier.size(28.dp))
        }

        // --- SCROLLABLE CONTENT ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- HEADER ---
            Text(
                text = if (isHindi) "आपकी स्क्रीनिंग\nका सारांश" else "Your Screening\nSummary",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textDark,
                lineHeight = 38.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isHindi) "हमने आपके वर्तमान भावनात्मक परिदृश्य को समझने में आपकी मदद करने के लिए आपके हालिया इनपुट का विश्लेषण किया है।" else "We've analyzed your recent inputs to help you understand your current emotional landscape.",
                fontSize = 15.sp,
                color = textGray,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- CURRENT STATUS CARD ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(if (isHindi) "वर्तमान स्थिति" else "CURRENT STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textGray, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(riskColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("$translatedRiskWord ${if (isHindi) "जोखिम" else "Risk"}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = riskColor)
                            Text(riskSubtitle, fontSize = 14.sp, color = textGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Custom Progress Bar
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFE0E5E2), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(progressFraction).height(6.dp).background(riskColor, RoundedCornerShape(50))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isHindi) "${(progressFraction * 100).toInt()}% तनाव संकेतक (अनुमानित सीमा)" else "${(progressFraction * 100).toInt()}% Stress Indicator (Estimated range)",
                        fontSize = 11.sp,
                        color = textGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- INSIGHTS CARD ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF8BB59F), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamically inject the AI's reason/suggestion
                    Text(
                        text = if (isHindi) "आपके उत्तरों के आधार पर, ${response.suggestion.lowercase()}" else "Based on your answers, ${response.suggestion.lowercase()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = response.reason, // NOTE: The API provides this text
                        fontSize = 15.sp,
                        color = textGray,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(if (isHindi) "दैनिक प्रतिज्ञान" else "Daily Affirmation", color = Color.White, fontSize = 13.sp)
                        }
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = bluePillBg),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(if (isHindi) "मूड दर्ज करें" else "Log Mood", color = bluePillText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- MICRO-BREAK CARD ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(64.dp).background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Air, contentDescription = null, tint = darkGreen, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (isHindi) "माइक्रो-ब्रेक के लिए तैयार हैं?" else "Ready for a micro-break?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (isHindi) "रीसेट करने के लिए 60 सेकंड का समय लें। अपनी सांसों पर ध्यान केंद्रित करने से आपके तनाव का स्तर और कम हो सकता है।" else "Take 60 seconds to reset. Focusing on your breath can further lower your cortisol levels.",
                        fontSize = 14.sp, color = textGray, textAlign = TextAlign.Center, lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (isHindi) "गाइडेड ब्रीदिंग शुरू करें →" else "Start Guided Breathing →", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = darkGreen)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- RECOMMENDED SECTION ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isHindi) "आपके लिए अनुशंसित" else "Recommended for You", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textDark)
                Text(if (isHindi) "सभी देखें" else "View All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = darkGreen)
            }
            Spacer(modifier = Modifier.height(16.dp))

            RecommendationItem(
                Icons.Default.WbSunny,
                if (isHindi) "मॉर्निंग जर्नलिंग" else "Morning Journaling",
                if (isHindi) "आज आप किन तीन चीजों के लिए आभारी हैं, उन्हें लिखें।" else "Write down three things you are grateful for today."
            )
            RecommendationItem(
                Icons.Default.NightsStay,
                if (isHindi) "नींद की दिनचर्या" else "Sleep Hygiene",
                if (isHindi) "बेहतर आराम के लिए अपनी शाम की दिनचर्या की समीक्षा करें।" else "Review your evening routine for better rest quality."
            )
            RecommendationItem(
                Icons.Default.Psychology,
                if (isHindi) "विचारों को नया रूप देना" else "Reframing Thoughts",
                if (isHindi) "नकारात्मक आत्म-चर्चा को सकारात्मक संवाद में बदलना सीखें।" else "Learn to turn negative self-talk into supportive dialogue."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- FOOTER DISCLAIMER ---
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F3)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFFA63C2E), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isHindi) "यह एक स्क्रीनिंग टूल है, क्लिनिकल निदान नहीं।\nयदि आप चिंतित हैं तो कृपया किसी विशेषज्ञ से परामर्श लें।" else "This is a screening tool, not a clinical diagnosis.\nPlease consult a healthcare professional if you're concerned.",
                        fontSize = 11.sp, fontStyle = FontStyle.Italic, color = textGray, textAlign = TextAlign.Center, lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(if (isHindi) "गोपनीयता नीति" else "Privacy Policy", fontSize = 10.sp, color = darkGreen, fontWeight = FontWeight.Bold)
                    Text(if (isHindi) "चिकित्सा अस्वीकरण" else "Medical Disclaimer", fontSize = 10.sp, color = darkGreen, fontWeight = FontWeight.Bold)
                    Text(if (isHindi) "देखभाल की शर्तें" else "Terms of Care", fontSize = 10.sp, color = darkGreen, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (isHindi) "© 2024 ManoDost AI • आपके मानसिक स्वास्थ्य के समर्थन में।" else "© 2024 ManoDost AI • Supporting your mental wellbeing.", fontSize = 9.sp, color = textGray)
            }
            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom nav
        }
    }

    // --- BOTTOM NAVIGATION BAR (Fixed at bottom) ---
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 12.dp).navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Icon (Navigates back)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onHomeClick() }) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF5E6D66))
                    Text(if (isHindi) "होम" else "Home", fontSize = 12.sp, color = Color(0xFF5E6D66), modifier = Modifier.padding(top = 4.dp))
                }

                // Chat Icon
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color(0xFF5E6D66))
                    Text(if (isHindi) "चैट" else "Chat", fontSize = 12.sp, color = Color(0xFF5E6D66), modifier = Modifier.padding(top = 4.dp))
                }

                // Insights Icon (ACTIVE)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(56.dp).background(darkGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = "Insights", tint = Color.White)
                    }
                    Text(if (isHindi) "इनसाइट्स" else "Insights", fontSize = 12.sp, color = darkGreen, modifier = Modifier.padding(top = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Reusable component for the Recommendation items
@Composable
fun RecommendationItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF1F5F3),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4B7B62), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1E21))
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = Color(0xFF65676B))
        }
    }
}