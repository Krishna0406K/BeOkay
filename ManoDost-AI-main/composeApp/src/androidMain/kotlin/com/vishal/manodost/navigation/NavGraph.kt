package com.vishal.manodost.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vishal.manodost.ui.AgeSelectionScreen
import com.vishal.manodost.ui.CallScreen
import com.vishal.manodost.ui.ChatScreen
import com.vishal.manodost.ui.DisclaimerScreen
import com.vishal.manodost.ui.HomeScreen
import com.vishal.manodost.ui.JuniorLoginScreen
import com.vishal.manodost.ui.LoginScreen
import com.vishal.manodost.ui.ProfileScreen
import com.vishal.manodost.ui.SplashScreen
import com.vishal.manodost.ui.screens.TheCircleScreen
import com.vishal.manodost.viewmodel.IChatViewModel
import com.vishal.manodost.ui.screens.TheCircleScreen
import com.vishal.manodost.viewmodel.ChatViewModel

@Composable
fun NavGraph(viewModel: IChatViewModel) {

    val navController = rememberNavController()
    
    // Create VoiceViewModel (needs context)
    val context = androidx.compose.ui.platform.LocalContext.current
    val voiceViewModel = remember { com.vishal.manodost.viewmodel.VoiceViewModel(context) }

    NavHost(navController, startDestination = "splash") {

        // 1. Splash Screen
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("disclaimer") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. Age Selection
        composable("age_selection") {
            AgeSelectionScreen(
                onAbove18Click = { navController.navigate("login") },
                onBelow18Click = { navController.navigate("junior_login") }
            )
        }

        // 3. Normal Login
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("disclaimer") {
                        popUpTo("age_selection") { inclusive = true }
                    }
                }
            )
        }

        // 4. Junior Login
        composable("junior_login") {
            JuniorLoginScreen(
                onLoginSuccess = {
                    navController.navigate("disclaimer") {
                        popUpTo("age_selection") { inclusive = true }
                    }
                }
            )
        }

        // 5. Disclaimer Screen
        composable("disclaimer") {
            DisclaimerScreen(
                onAccept = {
                    navController.navigate("home") {
                        popUpTo("disclaimer") { inclusive = true }
                    }
                }
            )
        }

        // 6. Home Screen
        composable("home") {
            HomeScreen(
                onChatClick = {
                    viewModel.startChat()
                    navController.navigate("chat") { launchSingleTop = true }
                },
                onCallClick = { navController.navigate("call") },
                onProfileClick = { navController.navigate("profile") },
                onCircleClick = {
                    navController.navigate("circle") { launchSingleTop = true }
                },
                onGamesClick = {
                    navController.navigate("games") { launchSingleTop = true }
                },
                onLanguageChange = { newLanguage ->
                    if (viewModel is com.vishal.manodost.viewmodel.ChatViewModelSimple) {
                        viewModel.updateLanguage(newLanguage)
                    }
                }
            )
        }

        // 7. Chat Screen
        composable("chat") {
            ChatScreen(
                viewModel = viewModel,
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onCircleClick = {
                    navController.navigate("circle") {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                }
            )
        }

        // 8. Call Screen (Voice AI)
        composable("call") {
            CallScreen(
                onEndCall = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                voiceViewModel = voiceViewModel
            )
        }

        // 9. The Circle Screen
        composable("circle") {
            TheCircleScreen(
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                // ✅ Added navigation back to Chat from Circle
                onChatClick = {
                    navController.navigate("chat") {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                }
            )
        }

        // 10. Profile Screen
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onSignOutClick = {
                    navController.navigate("age_selection") {
                        popUpTo(0)
                    }
                }
            )
        }
        
        // 11. Games Screen
        composable("games") {
            com.vishal.manodost.ui.screens.GamesScreen(
                onBackClick = { navController.popBackStack() },
                onGameClick = { gameId ->
                    navController.navigate("game_webview/$gameId")
                }
            )
        }
        
        // 12. Game WebView Screen
        composable("game_webview/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull() ?: 1
            com.vishal.manodost.ui.screens.GameWebViewScreen(
                gameId = gameId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}