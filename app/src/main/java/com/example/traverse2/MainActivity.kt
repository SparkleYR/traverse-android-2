package com.example.traverse2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.traverse2.data.api.RetrofitClient
import com.example.traverse2.service.StreakService
import com.example.traverse2.ui.screens.LoginScreen
import com.example.traverse2.ui.screens.MainScreen
import com.example.traverse2.ui.theme.TraverseTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, start the streak service
            startStreakService()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraverseTheme {
                TraverseApp(
                    onLoginSuccess = {
                        requestNotificationPermissionAndStartService()
                    }
                )
            }
        }
    }
    
    private fun requestNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    startStreakService()
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // No runtime permission needed for older Android versions
            startStreakService()
        }
    }
    
    private fun startStreakService() {
        // Fetch streak data from backend and start service
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getCurrentUser()
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) {
                        // Calculate hours remaining until streak expires (24 hours from now)
                        // In a real implementation, this should come from the backend
                        // which tracks the last solve time
                        val hoursRemaining = 24  // Placeholder - should be calculated from last solve time
                        
                        StreakService.startService(
                            context = this@MainActivity,
                            streakCount = user.currentStreak,
                            nextDeadlineHours = hoursRemaining
                        )
                    } else {
                        // User not logged in, start with default
                        StreakService.startService(
                            context = this@MainActivity,
                            streakCount = 0,
                            nextDeadlineHours = 24
                        )
                    }
                } else {
                    // Failed to fetch user data, start with default
                    StreakService.startService(
                        context = this@MainActivity,
                        streakCount = 0,
                        nextDeadlineHours = 24
                    )
                }
            } catch (e: Exception) {
                // Network error, start with default
                StreakService.startService(
                    context = this@MainActivity,
                    streakCount = 0,
                    nextDeadlineHours = 24
                )
            }
        }
    }
}

@Composable
fun TraverseApp(onLoginSuccess: () -> Unit = {}) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        }
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    onLoginSuccess()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            MainScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TraverseAppPreview() {
    TraverseTheme {
        TraverseApp()
    }
}