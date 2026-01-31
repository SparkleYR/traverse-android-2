package com.example.traverse2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.traverse2.data.SessionManager
import com.example.traverse2.data.api.RetrofitClient
import com.example.traverse2.data.model.User
import com.example.traverse2.ui.theme.GlassColors
import com.example.traverse2.ui.theme.ThemeState
import com.example.traverse2.ui.theme.TraverseTheme
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    hazeState: HazeState,
    onLogout: () -> Unit,
    user: User? = null,
    onEditProfile: () -> Unit = {},
    onFreezeShop: () -> Unit = {}
) {
    val glassColors = TraverseTheme.glassColors
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = SessionManager.getInstance(context)

    val username = user?.displayName ?: user?.username ?: "User"
    val email = user?.email ?: "No email"
    val dayStreak = user?.currentStreak ?: 0
    val totalXp = user?.totalXp ?: 0

    // Profile picture state
    var profilePicUrl by remember { mutableStateOf<String?>(null) }
    
    // Load cached profile pic or fetch new one
    LaunchedEffect(Unit) {
        val cachedUrl = sessionManager.getProfilePicUrlSync()
        if (cachedUrl != null) {
            profilePicUrl = cachedUrl
        } else {
            // Fetch a random cat pic and cache it
            val newUrl = "https://cataas.com/cat?${System.currentTimeMillis()}"
            sessionManager.saveProfilePicUrl(newUrl)
            profilePicUrl = newUrl
        }
    }

    val handleLogout: () -> Unit = {
        scope.launch {
            try {
                RetrofitClient.api.logout()
            } catch (e: Exception) { }
            sessionManager.clearSession()
            RetrofitClient.clearCookies()
            onLogout()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 120.dp)
    ) {
        // Header
        Text(
            text = "User",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = glassColors.textPrimary,
            letterSpacing = (-0.5).sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Card with cat picture
        ProfileCard(
            username = username,
            email = email,
            dayStreak = dayStreak,
            totalXp = totalXp,
            profilePicUrl = profilePicUrl,
            glassColors = glassColors
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Settings Grid (2x3)
        SettingsGrid(
            glassColors = glassColors,
            onEditProfile = onEditProfile,
            onLogout = handleLogout
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Freeze Shop Row
        SettingsRow(
            icon = Icons.Default.AcUnit,
            title = "Freeze Shop",
            glassColors = glassColors,
            showArrow = true,
            onClick = onFreezeShop
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Delete Account Row
        SettingsRow(
            icon = Icons.Default.Delete,
            title = "Delete Account",
            glassColors = glassColors,
            showArrow = true,
            onClick = { }
        )
    }
}

@Composable
private fun ProfileCard(
    username: String,
    email: String,
    dayStreak: Int,
    totalXp: Int,
    profilePicUrl: String?,
    glassColors: GlassColors
) {
    val backgroundColor = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(glassColors.textSecondary.copy(alpha = 0.2f))
                    .border(
                        width = 2.dp,
                        color = glassColors.textSecondary.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profilePicUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePicUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = glassColors.textSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username
            Text(
                text = username,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = glassColors.textPrimary,
                letterSpacing = (-0.3).sp
            )
            
            // Email
            Text(
                text = email,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = glassColors.textSecondary,
                letterSpacing = 0.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Day Streak
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$dayStreak",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Day Streak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = glassColors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
                
                // Total XP
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalXp",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Total XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = glassColors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGrid(
    glassColors: GlassColors,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val backgroundColor = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val dividerColor = glassColors.textSecondary.copy(alpha = 0.15f)
    val isDarkMode = ThemeState.isDarkMode
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
    ) {
        Column {
            // Row 1: Theme Toggle | Hue Picker
            Row(modifier = Modifier.fillMaxWidth()) {
                SettingsGridItem(
                    icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = if (isDarkMode) "Dark" else "Light",
                    subtitle = "Tap to switch",
                    glassColors = glassColors,
                    onClick = { ThemeState.toggleTheme() },
                    modifier = Modifier.weight(1f)
                )
                
                VerticalDivider(dividerColor)
                
                SettingsGridItem(
                    icon = Icons.Default.ColorLens,
                    title = "Hue Picker",
                    subtitle = "Coming soon",
                    glassColors = glassColors,
                    isDisabled = true,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
            }
            
            HorizontalDivider(color = dividerColor, thickness = 1.dp)
            
            // Row 2: Import | Profile
            Row(modifier = Modifier.fillMaxWidth()) {
                SettingsGridItem(
                    icon = Icons.Default.Download,
                    title = "Import",
                    subtitle = "Coming soon",
                    glassColors = glassColors,
                    isDisabled = true,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                
                VerticalDivider(dividerColor)
                
                SettingsGridItem(
                    icon = Icons.Default.Person,
                    title = "Profile",
                    subtitle = "Edit details",
                    glassColors = glassColors,
                    onClick = onEditProfile,
                    modifier = Modifier.weight(1f)
                )
            }
            
            HorizontalDivider(color = dividerColor, thickness = 1.dp)
            
            // Row 3: Security | Logout
            Row(modifier = Modifier.fillMaxWidth()) {
                SettingsGridItem(
                    icon = Icons.Default.Lock,
                    title = "Security",
                    subtitle = "Change password",
                    glassColors = glassColors,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                
                VerticalDivider(dividerColor)
                
                SettingsGridItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    subtitle = "Sign out",
                    glassColors = glassColors,
                    onClick = onLogout,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VerticalDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(110.dp)
            .background(color)
    )
}

@Composable
private fun SettingsGridItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    glassColors: GlassColors,
    isDisabled: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (isDisabled) 0.4f else 1f
    
    Column(
        modifier = modifier
            .height(110.dp)
            .clickable(enabled = !isDisabled, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Icon - monochrome
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = glassColors.textPrimary.copy(alpha = alpha),
            modifier = Modifier.size(26.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Title and subtitle
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = glassColors.textPrimary.copy(alpha = alpha),
                letterSpacing = (-0.2).sp
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = glassColors.textSecondary.copy(alpha = alpha),
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    glassColors: GlassColors,
    showArrow: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = glassColors.textPrimary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = glassColors.textPrimary,
            letterSpacing = (-0.2).sp,
            modifier = Modifier.weight(1f)
        )
        
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = glassColors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
