package com.vishal.manodost.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishal.manodost.data.model.CirclePost
import com.vishal.manodost.ui.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheCircleScreen(
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val isHindi = AppSettings.isHindi
    val circleText = Color(0xFF4B7B62)
    val darkGreen = Color(0xFF385E4D)
    val bodyTextDark = Color(0xFF1E1E1E)
    val lightBg = Color(0xFFF7F9F8)
    val actionIconColor = Color(0xFF6E6E6E)
    val pureWhite = Color.White

    val posts = remember {
        mutableStateListOf(
            CirclePost(1, Color(0xFF8BB59F), "2h ago", if (isHindi) "आज मैंने 10 मिनट ध्यान किया। बहुत शांति महसूस हुई।" else "I meditated for 10 minutes today. Felt so peaceful.", isUserPost = false),
            CirclePost(2, Color(0xFFE5B595), "4h ago", if (isHindi) "क्या किसी और को भी रात में नींद आने में दिक्कत होती है?" else "Does anyone else struggle with falling asleep at night?", isUserPost = false),
            CirclePost(3, Color(0xFF8CB9E1), "5h ago", if (isHindi) "काम का बोझ बहुत ज्यादा है, समझ नहीं आ रहा कहाँ से शुरू करूं।" else "Work pressure is immense, don't know where to start.", isUserPost = true),
            CirclePost(4, Color(0xFFD97762), "8h ago", if (isHindi) "गलतियां करना ठीक है, हम सब सीख रहे हैं।" else "It's okay to make mistakes, we are all learning.", isUserPost = false),
            CirclePost(5, Color(0xFFB5A4D9), "1d ago", if (isHindi) "मदद मांगना कमजोरी नहीं, ताकत है।" else "Asking for help isn't weakness, it's strength.", isUserPost = false)
        )
    }

    var showPostDialog by remember { mutableStateOf(false) }
    var showReplyDialog by remember { mutableStateOf(false) }
    var selectedPostForReply by remember { mutableStateOf<CirclePost?>(null) }
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        // ✅ FloatingActionButton REMOVED - Pencil is now in the top header
        bottomBar = {
            Surface(
                color = pureWhite,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onHomeClick() }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF5E6D66))
                        Text(if (isHindi) "होम" else "Home", fontSize = 12.sp, color = Color(0xFF5E6D66), modifier = Modifier.padding(top = 4.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onChatClick() }
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color(0xFF5E6D66))
                        Text(if (isHindi) "चैट" else "Chat", fontSize = 12.sp, color = Color(0xFF5E6D66), modifier = Modifier.padding(top = 4.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF638C7A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = "The Circle", tint = pureWhite)
                        }
                        Text(if (isHindi) "सर्कल" else "Circle", fontSize = 12.sp, color = darkGreen, modifier = Modifier.padding(top = 4.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->

        if (showPostDialog) {
            AlertDialog(
                onDismissRequest = { showPostDialog = false },
                title = { Text(if (isHindi) "विचार साझा करें" else "Share Thoughts") },
                text = {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text(if (isHindi) "कुछ लिखें..." else "Write something...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (inputText.isNotBlank()) {
                            posts.add(0, CirclePost(posts.size + 1, Color(0xFF6B8B79), "Just now", inputText, isUserPost = true))
                            showPostDialog = false
                        }
                    }) { Text(if (isHindi) "पोस्ट करें" else "Post", color = circleText, fontWeight = FontWeight.Bold) }
                }
            )
        }

        if (showReplyDialog && selectedPostForReply != null) {
            AlertDialog(
                onDismissRequest = { showReplyDialog = false },
                title = { Text(if (isHindi) "जवाब दें" else "Reply") },
                text = {
                    Column {
                        Text("\"${selectedPostForReply?.body}\"", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text(if (isHindi) "अपना जवाब लिखें..." else "Write your reply...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showReplyDialog = false }) {
                        Text(if (isHindi) "भेजें" else "Send", color = circleText, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().background(lightBg).padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
        ) {
            item {
                // ✅ UPDATED HEADER: Title and Pencil Icon perfectly aligned
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "द सर्कल" else "The Circle",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = circleText
                    )

                    // ✅ NEW POST BUTTON: Placed at the top right, straight aligned with title
                    IconButton(
                        onClick = {
                            inputText = ""
                            showPostDialog = true
                        },
                        modifier = Modifier
                            .background(Color(0xFFE8F0ED), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Post",
                            tint = circleText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(if (isHindi) "सांस लें,\nदिल की बात शेयर करें।" else "Breath in,\nshare your heart.", fontSize = 28.sp, color = bodyTextDark, lineHeight = 36.sp)
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(items = posts, key = { it.id }) { post ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart && post.isUserPost) {
                            posts.remove(post)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromEndToStart = post.isUserPost,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart && post.isUserPost) Color(0xFFD97762) else Color.Transparent
                        Box(modifier = Modifier.fillMaxSize().padding(bottom = 16.dp).background(color, RoundedCornerShape(24.dp)).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) {
                            if (post.isUserPost) Icon(Icons.Default.Delete, contentDescription = "Delete", tint = pureWhite)
                        }
                    }
                ) {
                    SimpleThoughtCard(
                        post = post,
                        actionIconColor = actionIconColor,
                        bodyTextDark = bodyTextDark,
                        onReplyClick = {
                            selectedPostForReply = post
                            inputText = ""
                            showReplyDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleThoughtCard(post: CirclePost, actionIconColor: Color, bodyTextDark: Color, onReplyClick: () -> Unit) {
    var isSupported by remember { mutableStateOf(false) }
    val heartColor = if (isSupported) Color(0xFFD97762) else actionIconColor

    Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).background(post.avatarColor, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(post.timeAgo, fontSize = 12.sp, color = Color.Gray)
                if (post.isUserPost) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                        Text("YOU", fontSize = 9.sp, color = Color(0xFF388E3C), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = post.body, fontSize = 15.sp, color = bodyTextDark, lineHeight = 22.sp)
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF1F3F2))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isSupported = !isSupported }
                ) {
                    Icon(
                        imageVector = if (isSupported) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Support",
                        tint = heartColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSupported) "Supported" else "Support",
                        fontSize = 13.sp,
                        color = heartColor,
                        fontWeight = if (isSupported) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onReplyClick() }) {
                    Icon(Icons.Default.Reply, contentDescription = null, tint = actionIconColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reply", fontSize = 13.sp, color = actionIconColor)
                }
            }
        }
    }
}