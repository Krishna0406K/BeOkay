package com.vishal.manodost.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vishal.manodost.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val pureWhite = Color.White
    val darkGreen = Color(0xFF638C7A)
    val textDark = Color(0xFF1C1E21)
    val textGray = Color(0xFF7A8A83)
    val inputBg = Color(0xFFF2F5F3)
    val lightBackground = Color(0xFFF4F9F6)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var isHindi by remember { mutableStateOf(AppSettings.isHindi) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Color.White, lightBackground)))
            .padding(horizontal = 24.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- 1. LANGUAGE TOGGLE ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(shape = RoundedCornerShape(50), color = inputBg, modifier = Modifier.height(36.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (!isHindi) pureWhite else Color.Transparent)
                            .clickable {
                                isHindi = false
                                AppSettings.isHindi = false
                                scope.launch {
                                    authViewModel.updateLanguage("en")
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("EN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isHindi) darkGreen else textGray)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isHindi) pureWhite else Color.Transparent)
                            .clickable {
                                isHindi = true
                                AppSettings.isHindi = true
                                scope.launch {
                                    authViewModel.updateLanguage("hi")
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("HI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isHindi) darkGreen else textGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. LOGO ICON ---
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(brush = Brush.linearGradient(colors = listOf(Color(0xFF8BB59F), Color(0xFF385E4D))), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Eco, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. TITLE ---
        Text(
            text = if (isHindi) "ManoDost AI में\nआपका स्वागत है" else "Welcome to ManoDost\nAI",
            fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = textDark, textAlign = TextAlign.Center, lineHeight = 38.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- ERROR MESSAGE ---
        if (errorMessage != null) {
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFC62828),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // --- 4. EMAIL FIELD ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(if (isHindi) "ईमेल या फोन नंबर" else "Email or Phone", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textDark)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("your@email.com", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBg, unfocusedContainerColor = inputBg,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = darkGreen
                ),
                singleLine = true,
                enabled = !isLoading
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. PASSWORD FIELD ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isHindi) "पासवर्ड" else "Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textDark)
                Text(if (isHindi) "पासवर्ड भूल गए?" else "Forgot Password?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = darkGreen, modifier = Modifier.clickable { })
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(if (isHindi) "अपना पासवर्ड दर्ज करें" else "Enter your password", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.Gray) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle Password", tint = Color.Gray)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBg, unfocusedContainerColor = inputBg,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = darkGreen
                ),
                singleLine = true,
                enabled = !isLoading
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- 6. SIGN IN BUTTON ---
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = if (isHindi) "कृपया सभी फ़ील्ड भरें" else "Please fill all fields"
                    return@Button
                }
                
                isLoading = true
                errorMessage = null
                
                scope.launch {
                    val result = authViewModel.signIn(email, password)
                    isLoading = false
                    
                    result.onSuccess {
                        onLoginSuccess()
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Login failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isHindi) "साइन इन करें" else "Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Sign In", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- 7. SIGN UP TEXT ---
        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(color = textGray)) {
                append(if (isHindi) "खाता नहीं है? " else "Don't have an account? ")
            }
            withStyle(style = SpanStyle(color = darkGreen, fontWeight = FontWeight.Bold)) {
                append(if (isHindi) "साइन अप करें" else "Sign up")
            }
        }
        Text(text = annotatedString, fontSize = 15.sp, modifier = Modifier.clickable { })

        Spacer(modifier = Modifier.weight(1f))

        // --- 8. FOOTER ---
        val footerText = if (isHindi) "गोपनीयता    शर्तें    सहायता" else "PRIVACY    TERMS    SUPPORT"
        Text(
            footerText, fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}
