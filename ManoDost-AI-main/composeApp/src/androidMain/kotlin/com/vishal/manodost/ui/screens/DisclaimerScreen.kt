package com.vishal.manodost.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DisclaimerScreen(onAccept: () -> Unit) {

    val darkGreen = Color(0xFF385E4D)
    val lightBackground = Color(0xFFF2F7F4)
    val textGray = Color(0xFF7A8A83)
    val pureWhite = Color.White

    // ✅ 1. Read the global language state
    val isHindi = AppSettings.isHindi

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, lightBackground)
                )
            )
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ 2. Translate Title
        Text(
            text = if (isHindi) "सचेत उपयोग" else "Mindful Usage",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = darkGreen,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ✅ 3. Translate Subtitle
        Text(
            text = if (isHindi) "शुरू करने से पहले, आपकी भलाई के लिए कुछ ज़रूरी बातें।"
            else "Before we begin, a few gentle reminders for your well-being.",
            fontSize = 16.sp,
            color = textGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 48.dp) // Slightly increased spacing for the simpler look
        )

        // 🎨 SIMPLIFIED DESIGN: Removed the bulky Surface card so items float cleanly
        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp) // Increased spacing between items
        ) {
            DisclaimerItem(
                icon = Icons.Default.Psychology,
                title = if (isHindi) "AI है, डॉक्टर नहीं" else "AI, Not a Doctor",
                description = if (isHindi) "ManoDost एक AI साथी है। यह पेशेवर थेरेपी या डॉक्टरी सलाह का विकल्प नहीं है।"
                else "ManoDost is an AI companion. It is not a replacement for professional therapy or medical diagnosis."
            )

            DisclaimerItem(
                icon = Icons.Default.LocalDrink,
                title = if (isHindi) "डिजिटल संतुलन" else "Digital Balance",
                description = if (isHindi) "कृपया इस ऐप का अत्यधिक उपयोग न करें। स्क्रीन से ब्रेक लेना, स्ट्रेच करना और पानी पीना याद रखें।"
                else "Please do not use this app excessively. Remember to take screen breaks, stretch, and drink a glass of water."
            )

            DisclaimerItem(
                icon = Icons.Default.Favorite,
                title = if (isHindi) "वास्तविक दुनिया से जुड़ाव" else "Real-World Connection",
                description = if (isHindi) "हालांकि हम यहाँ सुनने के लिए हैं, कृपया सुनिश्चित करें कि आप अपने दोस्तों, परिवार और वास्तविक दुनिया से भी जुड़े रहें।"
                else "While we are here to listen, please ensure you also stay connected with friends, family, and the real world."
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 4. ACCEPT BUTTON ---
        Button(
            onClick = onAccept,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            // ✅ Translate Button
            Text(
                text = if (isHindi) "मुझे समझ आ गया" else "I Understand",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = pureWhite
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Reusable component for the list items
@Composable
fun DisclaimerItem(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFE8EFEA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF385E4D),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1E21),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF65676B), // Slightly darker gray for better readability without the card
                lineHeight = 20.sp
            )
        }
    }
}