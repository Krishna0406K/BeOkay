package com.vishal.manodost.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    // Automatically navigate after 2.5 seconds
    LaunchedEffect(key1 = true) {
        delay(2500)
        onSplashFinished()
    }

    // Exact colors from your design
    val darkGreen = Color(0xFF385E4D)
    val lightBackground = Color(0xFFF4F9F6)
    val innerCircleColor = Color(0xFFE8EFEA)
    val textGray = Color(0xFF8B9D94)

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

        Spacer(modifier = Modifier.weight(1f)) // Pushes content to the center

        // --- 1. LAYERED LOGO ---
        Box(
            modifier = Modifier
                .size(180.dp)
                .shadow(elevation = 16.dp, shape = CircleShape, ambientColor = darkGreen, spotColor = darkGreen)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(innerCircleColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco, // A perfect leaf icon
                    contentDescription = "ManoDost Logo",
                    tint = darkGreen,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- 2. TITLE (Wide Tracking) ---
        Text(
            text = "BeOkay",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = darkGreen,
            letterSpacing = 6.sp, // Creates that premium, spaced-out look
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. SUBTITLE ---
        Text(
            text = "Your digital sanctuary.",
            fontSize = 18.sp,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif,
            color = textGray,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.weight(1f)) // Pushes the loader to the bottom

        // --- 4. BOTTOM LOADER ---
        CircularProgressIndicator(
            color = darkGreen,
            strokeWidth = 2.dp, // Thin, elegant stroke
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PREPARING YOUR SAFE SPACE",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textGray,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 48.dp) // Keeps it off the very edge
        )
    }
}