package com.example.traverse2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traverse2.ui.components.GlassTopBar
import com.example.traverse2.ui.theme.GlassColors
import com.example.traverse2.ui.theme.TraverseTheme
import dev.chrisbanes.haze.HazeState

@Composable
fun AchievementsScreen(
    hazeState: HazeState,
    onBack: () -> Unit,
    achievements: List<AchievementData> = emptyList()
) {
    val glassColors = TraverseTheme.glassColors
    
    val unlockedCount = achievements.count { it.unlocked }
    val totalCount = achievements.size
    val remaining = totalCount - unlockedCount
    val percentage = if (totalCount > 0) (unlockedCount * 100 / totalCount) else 0
    
    // Group achievements by category
    val achievementsByCategory = achievements.groupBy { it.category }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        if (glassColors.isDark) Color(0xFF000000) else Color(0xFFf5f5f7),
                        if (glassColors.isDark) Color(0xFF0A0A0A) else Color(0xFFe8e8ed)
                    )
                )
            )
    ) {
        // Top bar
        GlassTopBar(
            hazeState = hazeState,
            glassColors = glassColors,
            onBack = onBack,
            title = "All Achievements"
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StatsHeader(
                    total = totalCount,
                    progress = percentage,
                    remaining = remaining,
                    glassColors = glassColors
                )
            }
            
            // Section title
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Achievements by Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.textPrimary
                )
            }
            
            // Category cards
            items(achievementsByCategory.toList()) { (category, categoryAchievements) ->
                CategoryCard(
                    category = category,
                    achievements = categoryAchievements,
                    glassColors = glassColors
                )
            }
            
            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun StatsHeader(
    total: Int,
    progress: Int,
    remaining: Int,
    glassColors: GlassColors
) {
    val cardBg = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val textColor = glassColors.textPrimary
    val secondaryColor = glassColors.textSecondary
    val accentColor = Color(0xFFB388FF) // Purple accent
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$total",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = "Total",
                    fontSize = 13.sp,
                    color = secondaryColor
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(secondaryColor.copy(alpha = 0.3f))
            )
            
            // Progress
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$progress%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = "Progress",
                    fontSize = 13.sp,
                    color = secondaryColor
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(secondaryColor.copy(alpha = 0.3f))
            )
            
            // Remaining
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$remaining",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "Remaining",
                    fontSize = 13.sp,
                    color = secondaryColor
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: String,
    achievements: List<AchievementData>,
    glassColors: GlassColors
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val unlockedCount = achievements.count { it.unlocked }
    val totalCount = achievements.size
    
    val cardBg = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val iconBg = if (glassColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expandRotation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = category,
                        tint = glassColors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Category info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.replaceFirstChar { it.uppercase() },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = glassColors.textPrimary
                    )
                    Text(
                        text = "$unlockedCount of $totalCount unlocked",
                        fontSize = 14.sp,
                        color = glassColors.textSecondary
                    )
                }
                
                // Expand arrow
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = glassColors.textSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                )
            }
            
            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    achievements.forEachIndexed { index, achievement ->
                        AchievementRow(
                            achievement = achievement,
                            glassColors = glassColors
                        )
                        if (index < achievements.lastIndex) {
                            HorizontalDivider(
                                color = glassColors.textSecondary.copy(alpha = 0.15f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AchievementRow(
    achievement: AchievementData,
    glassColors: GlassColors
) {
    val iconBg = if (glassColors.isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val lockedAlpha = 0.5f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Achievement icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (achievement.unlocked) 
                    glassColors.textPrimary 
                else 
                    glassColors.textSecondary.copy(alpha = lockedAlpha),
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Achievement info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (achievement.unlocked) 
                    glassColors.textPrimary 
                else 
                    glassColors.textSecondary.copy(alpha = 0.7f)
            )
            Text(
                text = achievement.description,
                fontSize = 13.sp,
                color = glassColors.textSecondary.copy(
                    alpha = if (achievement.unlocked) 1f else 0.6f
                ),
                lineHeight = 17.sp
            )
        }
    }
}
