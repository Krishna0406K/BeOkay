package com.vishal.manodost.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JuniorLoginScreen(onLoginSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }

    val darkGreen = Color(0xFF385E4D)

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp)) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("Hello there!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = darkGreen)
        Text("Let's get you started with ManoDost.", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Your Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = parentPhone, onValueChange = { parentPhone = it }, label = { Text("Parent's Phone Number") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            shape = RoundedCornerShape(50)
        ) {
            Text("Start My Journey", fontSize = 18.sp)
        }
    }
}