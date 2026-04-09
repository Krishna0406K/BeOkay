package com.vishal.manodost.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCircleClick: () -> Unit,
    onGamesClick: () -> Unit,
    onLanguageChange: (String) -> Unit = {}
) {

    val darkGreen = Color(0xFF385E4D)
    val lightBackground = Color(0xFFF2F7F4)
    val subtitleGray = Color(0xFF7A8A83)
    val pureWhite = Color.White

    val isHindi = AppSettings.isHindi

    val quotes = if (isHindi) {
        listOf(
            "आप अकेले नहीं हैं।\nआइए मिलकर इस पर\nबात करें।",
            "ठीक होने में समय लगता है,\nऔर यह बिल्कुल सामान्य है।",
            "आपका मानसिक स्वास्थ्य\nहमारी प्राथमिकता है।",
            "गहरी सांस लें।\nआप यहां सुरक्षित हैं।",
            "मदद मांगना\nसाहस की निशानी है।"
        )
    } else {
        listOf(
            "You're not alone.\nLet's talk it through\ntogether.",
            "Healing takes time,\nand that is okay.",
            "Your mental health\nis a priority.",
            "Take a deep breath.\nYou are safe here.",
            "Asking for help is\na sign of courage."
        )
    }

    var currentQuoteIndex by remember { mutableStateOf(0) }
    var displayedText by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "cursorAlpha"
    )

    LaunchedEffect(currentQuoteIndex, isHindi) {
        val quoteToType = quotes[currentQuoteIndex]
        displayedText = ""
        for (char in quoteToType) {
            displayedText += char
            delay(50)
        }
        delay(3000)
        currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = listOf(Color.White, lightBackground)))
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TOP APP BAR ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = darkGreen, modifier = Modifier.size(28.dp))
                Text("BeOkay", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = darkGreen)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Toggle Button
                    Surface(
                        modifier = Modifier.clickable {
                            val newLanguage = if (isHindi) "en" else "hi"
                            AppSettings.isHindi = !isHindi
                            onLanguageChange(newLanguage)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE5ECE9),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Language",
                                tint = darkGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isHindi) "EN" else "हिं",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkGreen
                            )
                        }
                    }
                    
                    // Profile Button
                    Box(
                        modifier = Modifier.size(36.dp).background(Color(0xFFE5ECE9), CircleShape).clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = darkGreen, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // --- CENTER ICON ---
            Surface(modifier = Modifier.size(72.dp), shape = CircleShape, color = pureWhite, shadowElevation = 8.dp) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("🌿", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MAIN QUOTE ---
            Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    text = displayedText + if (cursorAlpha > 0.5f) "|" else "",
                    fontSize = 34.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Serif, color = darkGreen, textAlign = TextAlign.Center, lineHeight = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isHindi) "शांत आत्म-चिंतन और\nसहयोगी बातचीत के लिए\nआपका निजी डिजिटल आश्रय।"
                else "Your private, digital sanctuary for\nquiet reflection and supportive\nconversation.",
                fontSize = 16.sp, color = subtitleGray, textAlign = TextAlign.Center, lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- BUTTONS ---
            Button(
                onClick = onCallClick,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = darkGreen)
            ) {
                Text(text = if (isHindi) "🎙️  कॉल पर बात करें" else "🎙️  Talk on Call", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = pureWhite)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = onChatClick,
                modifier = Modifier.fillMaxWidth().height(64.dp).border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(50)),
                shape = RoundedCornerShape(50),
                color = pureWhite,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = if (isHindi) "💬  टेक्स्ट द्वारा चैट करें" else "💬  Chat via Text", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = darkGreen)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = onGamesClick,
                modifier = Modifier.fillMaxWidth().height(64.dp).border(1.dp, Color(0xFFAEE1C1), RoundedCornerShape(50)),
                shape = RoundedCornerShape(50),
                color = Color(0xFFF0F9F4),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = if (isHindi) "🎮  मानसिक स्वास्थ्य खेल" else "🎮  Wellness Games", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = darkGreen)
                }
            }

            Spacer(modifier = Modifier.height(120.dp)) // Leave space for Bottom Nav
        }

        // --- UPDATED BOTTOM NAVIGATION BAR ---
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = pureWhite,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 12.dp).navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home (Active)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(56.dp).background(Color(0xFF638C7A), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = pureWhite)
                    }
                    Text(if (isHindi) "होम" else "Home", fontSize = 12.sp, color = darkGreen, modifier = Modifier.padding(top = 4.dp), fontWeight = FontWeight.Bold)
                }

                // 2. Chat
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onChatClick() }) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color(0xFF5E6D66))
                    Text(if (isHindi) "चैट" else "Chat", fontSize = 12.sp, color = Color(0xFF5E6D66), modifier = Modifier.padding(top = 4.dp))
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