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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val isHindi = AppSettings.isHindi
    val isDark = ThemeSettings.isDarkMode

    // --- DIALOG STATES ---
    var showSecureDataDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Colors that adapt to Theme
    val bgColor = if (isDark) Color(0xFF121212) else Color(0xFFF4F9F6)
    val cardColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1C1E21)
    val subTextColor = if (isDark) Color.LightGray else Color.Gray
    val primaryGreen = Color(0xFF638C7A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isHindi) "प्रोफ़ाइल" else "Profile", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- PROFILE HEADER ---
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(primaryGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Branding update to ManoVeer
            Text("Vishal Sharma", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(if (isHindi) "ManoVeer वॉरियर" else "ManoVeer Warrior", fontSize = 14.sp, color = primaryGreen, fontWeight = FontWeight.Medium)
            Text("vishal@manoveer.ai", fontSize = 12.sp, color = subTextColor)

            Spacer(modifier = Modifier.height(32.dp))

            // --- SETTINGS LIST ---
            ProfileItem(
                icon = Icons.Default.Shield,
                label = if (isHindi) "सुरक्षित डेटा" else "Secure Data",
                textColor = textColor,
                cardColor = cardColor,
                onClick = { showSecureDataDialog = true }
            )

            ProfileItem(
                icon = Icons.Default.PhoneInTalk,
                label = if (isHindi) "आपातकालीन संपर्क" else "Emergency Contacts",
                textColor = textColor,
                cardColor = cardColor,
                onClick = { showEmergencyDialog = true }
            )

            ProfileItem(
                // Dynamic Icon based on current theme
                icon = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                label = if (isHindi) "थीम बदलें" else "Change Theme",
                textColor = textColor,
                cardColor = cardColor,
                onClick = { showThemeDialog = true }
            )

            Spacer(modifier = Modifier.weight(1f))

            // SIGN OUT
            Button(
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97762)),
                shape = RoundedCornerShape(50)
            ) {
                Text(if (isHindi) "साइन आउट" else "Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ManoVeer v1.0.4",
                fontSize = 10.sp,
                color = subTextColor
            )
        }

        // --- 1. SECURE DATA DIALOG ---
        if (showSecureDataDialog) {
            AlertDialog(
                onDismissRequest = { showSecureDataDialog = false },
                title = { Text(if (isHindi) "डेटा सुरक्षा" else "Data Security") },
                text = {
                    Text(if (isHindi)
                        "ManoVeer में आपका डेटा पूरी तरह एन्क्रिप्टेड है। हम आपकी बातचीत की गोपनीयता का सम्मान करते हैं और इसे कभी साझा नहीं करते।"
                    else "In ManoVeer, your data is end-to-end encrypted. We respect your privacy and never share your chat history.")
                },
                confirmButton = {
                    TextButton(onClick = { showSecureDataDialog = false }) {
                        Text("OK", color = primaryGreen)
                    }
                }
            )
        }

        // --- 2. EMERGENCY CONTACTS DIALOG ---
        if (showEmergencyDialog) {
            AlertDialog(
                onDismissRequest = { showEmergencyDialog = false },
                title = { Text(if (isHindi) "आपातकालीन मदद" else "Emergency Help") },
                text = {
                    Column {
                        Text("• Vandrevala Foundation: 9999666555", fontWeight = FontWeight.Bold)
                        Text("• NIMHANS (24x7): 080-46110007")
                        Text("• AASRA: 9820466726")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(if (isHindi) "यदि आप खतरे में हैं, तो कृपया इन नंबरों पर कॉल करें। आप अकेले नहीं हैं।" else "If you are in immediate danger, please call these helplines. You are not alone.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showEmergencyDialog = false }) {
                        Text("OK", color = primaryGreen)
                    }
                }
            )
        }

        // --- 3. THEME SELECTOR DIALOG ---
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text(if (isHindi) "थीम चुनें" else "Choose Theme") },
                text = {
                    Column {
                        ThemeOptionRow(
                            label = if (isHindi) "लाइट मोड" else "Light Mode",
                            isSelected = !isDark,
                            onClick = {
                                ThemeSettings.isDarkMode = false
                                showThemeDialog = false
                            }
                        )
                        ThemeOptionRow(
                            label = if (isHindi) "डार्क मोड" else "Dark Mode",
                            isSelected = isDark,
                            onClick = {
                                ThemeSettings.isDarkMode = true
                                showThemeDialog = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text(if (isHindi) "बंद करें" else "Close", color = primaryGreen)
                    }
                }
            )
        }
    }
}

@Composable
fun ThemeOptionRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF638C7A))
        )
        Text(label, modifier = Modifier.padding(start = 12.dp), fontSize = 16.sp)
    }
}

@Composable
fun ProfileItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, textColor: Color, cardColor: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF638C7A))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontSize = 16.sp, color = textColor)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}