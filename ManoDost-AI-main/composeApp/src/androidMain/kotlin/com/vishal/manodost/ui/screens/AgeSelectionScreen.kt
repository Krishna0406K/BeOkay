package com.vishal.manodost.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AgeSelectionScreen(onAbove18Click: () -> Unit, onBelow18Click: () -> Unit) {
    val darkGreen = Color(0xFF385E4D)
    val lightBg = Color(0xFFF2F7F4)

    Column(
        modifier = Modifier.fillMaxSize().background(lightBg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to ManoDost", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = darkGreen)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Please select your age group to continue", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onAbove18Click,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("I am 18 or older", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBelow18Click,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = darkGreen)
        ) {
            Text("I am under 18", fontSize = 18.sp)
        }
    }
}