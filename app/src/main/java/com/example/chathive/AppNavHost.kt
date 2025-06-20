package com.example.chathive

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chathive.screens.ChatScreen
import com.example.chathive.screens.LoginScreen
import com.example.chathive.screens.ProfileScreen
import com.example.chathive.screens.SearchScreen


@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("main") {
            SearchScreen(navController)
        }
        composable("chat/{chatUid}", arguments = listOf(navArgument("chatUid") { type = NavType.StringType })) { backStackEntry ->
            val chatUid = backStackEntry.arguments?.getString("chatUid") ?: ""
            ChatScreen(navController, chatUid)
        }
    }
}
