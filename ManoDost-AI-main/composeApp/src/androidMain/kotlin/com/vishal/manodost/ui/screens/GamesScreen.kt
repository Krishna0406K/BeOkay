package com.vishal.manodost.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishal.manodost.data.model.GamesData
import com.vishal.manodost.ui.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onBackClick: () -> Unit,
    onGameClick: (Int) -> Unit
) {
    val isHindi = AppSettings.isHindi
    val darkGreen = Color(0xFF1E3A2D)
    val lightBackground = Color(0xFFF4F9F6)
    val mintGreen = Color(0xFFAEE1C1)
    val cardBackground = Color(0xFFFFFFFF)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "मानसिक स्वास्थ्य खेल" else "Mental Wellness Games",
                        fontWeight = FontWeight.Bold,
                        color = darkGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = darkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, lightBackground)
                    )
                )
                .padding(padding)
        ) {
            // Header
            Text(
                text = if (isHindi) "खेलें और आराम करें" else "Play & Relax",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )

            // Games List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(GamesData.games) { game ->
                    GameCard(
                        game = game,
                        isHindi = isHindi,
                        onClick = { onGameClick(game.id) },
                        darkGreen = darkGreen,
                        mintGreen = mintGreen,
                        cardBackground = cardBackground
                    )
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: com.vishal.manodost.data.model.Game,
    isHindi: Boolean,
    onClick: () -> Unit,
    darkGreen: Color,
    mintGreen: Color,
    cardBackground: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(mintGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.icon,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Game Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = mintGreen.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = game.benefit,
                        fontSize = 12.sp,
                        color = darkGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Play Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = mintGreen,
                modifier = Modifier.clickable(onClick = onClick)
            ) {
                Text(
                    text = if (isHindi) "खेलें" else "Play",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkGreen,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
