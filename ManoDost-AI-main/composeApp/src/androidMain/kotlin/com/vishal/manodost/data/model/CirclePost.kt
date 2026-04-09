package com.vishal.manodost.data.model

data class CirclePost(
    val id: Int,
    val avatarColor: androidx.compose.ui.graphics.Color,
    val timeAgo: String,
    val body: String,
    val isUserPost: Boolean = false // ✅ Added this to control deletion
)