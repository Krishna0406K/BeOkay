package com.vishal.manodost.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun MessageInput(onSend: (String) -> Unit) {

    var text by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxWidth()) {

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f)
        )

        Button(onClick = {
            if (text.isNotBlank()) {
                onSend(text)
                text = ""
            }
        }) {
            Text("Send")
        }
    }
}