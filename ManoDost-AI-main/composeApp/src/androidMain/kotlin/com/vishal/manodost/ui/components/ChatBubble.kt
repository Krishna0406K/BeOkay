package com.vishal.manodost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val darkGreen = Color(0xFF385E4D)
    val aiBubbleColor = Color(0xFFF7F9F8)
    val pureWhite = Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {

        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // AI Icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(darkGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = pureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ManoDost AI",
                    fontSize = 12.sp,
                    color = darkGreen,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        } else {
            Text(
                text = "You",
                fontSize = 12.sp,
                color = darkGreen,
                modifier = Modifier.padding(bottom = 6.dp, end = 4.dp)
            )
        }

        val bubbleShape = if (isUser) {
            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 4.dp)
        } else {
            RoundedCornerShape(topStart = 4.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
        }

        Surface(
            color = if (isUser) darkGreen else aiBubbleColor,
            shape = bubbleShape,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Text(
                text = text,
                color = if (isUser) pureWhite else Color(0xFF1C1E21),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}