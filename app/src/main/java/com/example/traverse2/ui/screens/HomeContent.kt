package com.example.traverse2.ui.screens
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.LaunchedEffect
import com.example.traverse2.data.api.Solve
import com.example.traverse2.ui.theme.GlassColors
import com.example.traverse2.ui.theme.TraverseTheme
import com.example.traverse2.ui.viewmodel.HomeViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.LocalDate
import com.example.traverse2.ui.components.StreakDay

data class DifficultyData(val easy: Int, val medium: Int, val hard: Int)
data class PlatformData(val name: String, val count: Int, val color: Color)
data class SubmissionStats(val accepted: Int, val failed: Int, val total: Int)
data class CategoryData(val name: String, val count: Int)
data class RecentSolve(val problemName: String, val platform: String, val difficulty: String, val timeAgo: String)
data class AchievementData(val name: String, val description: String, val icon: String?, val category: String, val unlocked: Boolean)
data class ProblemItem(val name: String, val platform: String, val difficulty: String, val solved: Boolean)
data class StreakData(val currentStreak: Int, val longestStreak: Int, val totalActiveDays: Int, val averagePerWeek: Float, val streakDays: List<StreakDay> = emptyList())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    hazeState: HazeState,
    onLogout: () -> Unit,
    onNavigateToProblems: () -> Unit = {},
    onNavigateToStreak: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onStreakDataReady: (StreakData) -> Unit = {},
    onAchievementsDataReady: (List<AchievementData>) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val glassColors = TraverseTheme.glassColors
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Show loading indicator
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = glassColors.textPrimary)
        }
        return
    }
    
    // Show error state
    if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Error: ${uiState.error}", color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to retry",
                    color = glassColors.textSecondary,
                    modifier = Modifier.clickable { viewModel.refresh() }
                )
            }
        }
        return
    }
    
    // Extract data from state
    val user = uiState.user ?: return
    val solveStats = uiState.solveStats
    val submissionStats = uiState.submissionStats
    val recentSolves = uiState.recentSolves
    val achievements = uiState.achievements
    
    // Convert backend data to UI models
    val difficultyData = if (solveStats != null) {
        DifficultyData(
            easy = solveStats.byDifficulty["easy"] ?: 0,
            medium = solveStats.byDifficulty["medium"] ?: 0,
            hard = solveStats.byDifficulty["hard"] ?: 0
        )
    } else {
        DifficultyData(0, 0, 0)
    }
    
    val platforms = if (solveStats != null) {
        solveStats.byPlatform.map { (name, count) ->
            val color = if (glassColors.isDark) Color.White else Color.Black
            PlatformData(name, count, color)
        }
    } else {
        emptyList()
    }
    
    val submissionStatsData = if (submissionStats != null) {
        SubmissionStats(
            accepted = submissionStats.accepted,
            failed = submissionStats.failed,
            total = submissionStats.total
        )
    } else {
        null
    }
    
    val recentSolvesData = recentSolves.map { solve ->
        val timeAgo = calculateTimeAgo(solve.solvedAt)
        RecentSolve(
            problemName = solve.problem.title ?: solve.problem.slug,
            platform = solve.problem.platform,
            difficulty = solve.problem.difficulty ?: "Unknown",
            timeAgo = timeAgo
        )
    }
    
    val achievementsData = achievements.map { achievement ->
        AchievementData(
            name = achievement.name,
            description = achievement.description,
            icon = achievement.icon ?: "trophy",
            category = achievement.category,
            unlocked = achievement.unlocked
        )
    }
    
    // Get all solves for charts
    val allSolves = uiState.allSolves
    
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 60.dp, bottom = 120.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stay curious, keep coding!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.textPrimary
                )
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = glassColors.textSecondary
                    )
                }
            }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        StreakCard(
            streak = user.currentStreak,
            hazeState = hazeState,
            glassColors = glassColors,
            onClick = {
                    // Generate streak days from calendarSolveDates
                    val today = LocalDate.now()
                    val calendarSolveDates = uiState.calendarSolveDates
                    val streakDays = (0 until 35).map { daysAgo ->
                        val date = today.minusDays(daysAgo.toLong())
                        StreakDay(
                            date = date,
                            isActive = calendarSolveDates.contains(date),
                            isToday = daysAgo == 0
                        )
                    }
                    onStreakDataReady(
                        StreakData(
                            currentStreak = user.currentStreak,
                            longestStreak = user.longestStreak,
                            totalActiveDays = solveStats?.totalStreakDays ?: 0,
                            averagePerWeek = if (solveStats != null) solveStats.totalSolves / 4f else 0f,
                            streakDays = streakDays
                        )
                    )
                }
            )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        YourWorkCard(
            totalSolves = solveStats?.totalSolves ?: 0,
            totalXp = user.totalXp,
            streak = user.currentStreak,
            hazeState = hazeState,
            glassColors = glassColors
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Difficulty (circular) and Achievements side by side
        if (difficultyData.easy + difficultyData.medium + difficultyData.hard > 0 || achievementsData.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (difficultyData.easy + difficultyData.medium + difficultyData.hard > 0) {
                    DifficultyDonutCard(
                        difficultyData, 
                        hazeState, 
                        glassColors,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                
                AchievementsCompactCard(
                    achievements = achievementsData,
                    hazeState = hazeState,
                    glassColors = glassColors,
                    onClick = { onAchievementsDataReady(achievementsData) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        if (platforms.isNotEmpty()) {
            PlatformsCard(platforms, hazeState, glassColors)
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Mistake Analysis Card - using mistake tags from solves
        MistakeAnalysisCard(allSolves, hazeState, glassColors)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Time Performance Chart Card - using real solve data
        TimePerformanceChartCard(allSolves, hazeState, glassColors)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Attempts Analysis Chart Card - using real solve data
        AttemptsAnalysisCard(allSolves, hazeState, glassColors)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (recentSolvesData.isNotEmpty()) {
            RecentSolvesCard(
                solves = recentSolvesData, 
                hazeState = hazeState, 
                glassColors = glassColors,
                onClick = onNavigateToProblems
            )
        }
        }
    }
}

// Helper function to calculate time ago
private fun calculateTimeAgo(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours = ChronoUnit.HOURS.between(instant, now)
        val days = ChronoUnit.DAYS.between(instant, now)
        
        when {
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            days < 30 -> "${days / 7}w ago"
            else -> "${days / 30}mo ago"
        }
    } catch (e: Exception) {
        "Recently"
    }
}

@Composable
private fun GlassCardContainer(
    hazeState: HazeState,
    glassColors: GlassColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val backgroundColor = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .padding(24.dp)
    ) {
        content()
    }
}

@Composable
private fun GlassSubsection(
    glassColors: GlassColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val subsectionBg = if (glassColors.isDark) Color(0x20FFFFFF) else Color(0x30FFFFFF)
    val borderColor = if (glassColors.isDark) Color(0x15FFFFFF) else Color(0x20000000)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(subsectionBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun CardHeader(title: String, icon: ImageVector, glassColors: GlassColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = glassColors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = glassColors.textPrimary
        )
    }
}

@Composable
private fun StreakCard(streak: Int, hazeState: HazeState, glassColors: GlassColors, onClick: () -> Unit = {}) {
    val streakComment = when {
        streak == 0 -> "Start your streak today!"
        streak < 3 -> "Good start! Keep it going!"
        streak < 7 -> "You're building momentum!"
        streak < 14 -> "One week strong! Amazing!"
        streak < 30 -> "Unstoppable! Keep pushing!"
        else -> "Legendary coder! $streak days!"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (glassColors.isDark) Color.White else Color.Black)
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = if (glassColors.isDark) Color.Black else Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "$streak", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = if (glassColors.isDark) Color.Black else Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "day streak", fontSize = 18.sp, color = (if (glassColors.isDark) Color.Black else Color.White).copy(alpha = 0.9f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = streakComment, fontSize = 14.sp, color = (if (glassColors.isDark) Color.Black else Color.White).copy(alpha = 0.85f))
                Text(text = "Tap for details", fontSize = 12.sp, color = (if (glassColors.isDark) Color.Black else Color.White).copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun YourWorkCard(
    totalSolves: Int,
    totalXp: Int,
    streak: Int,
    hazeState: HazeState,
    glassColors: GlassColors
) {
    GlassCardContainer(hazeState = hazeState, glassColors = glassColors) {
        Column {
            CardHeader(title = "Your Work", icon = Icons.Default.Code, glassColors = glassColors)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatColumn(Icons.Default.CheckCircle, totalSolves.toString(), "Total Solves", glassColors)
                StatColumn(Icons.Default.Star, totalXp.toString(), "Total XP", glassColors)
                StatColumn(Icons.Default.LocalFireDepartment, streak.toString(), "Streak", glassColors)
            }
        }
    }
}

@Composable
private fun StatColumn(icon: ImageVector, value: String, label: String, glassColors: GlassColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, tint = glassColors.textSecondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = glassColors.textPrimary)
        Text(label, fontSize = 12.sp, color = glassColors.textSecondary)
    }
}

@Composable
private fun DifficultyDonutCard(
    data: DifficultyData, 
    hazeState: HazeState, 
    glassColors: GlassColors,
    modifier: Modifier = Modifier
) {
    val total = data.easy + data.medium + data.hard
    
    val easyColor = Color(0xFFB388FF) // Light purple
    val mediumColor = Color(0xFFFF80AB) // Pink  
    val hardColor = Color(0xFF82B1FF) // Light blue
    
    val backgroundColor = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Difficulty",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = glassColors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Donut Chart
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    
                    if (total > 0) {
                        val easyAngle = (data.easy.toFloat() / total) * 360f
                        val mediumAngle = (data.medium.toFloat() / total) * 360f
                        val hardAngle = (data.hard.toFloat() / total) * 360f
                        
                        var startAngle = -90f
                        
                        // Easy arc
                        drawArc(
                            color = easyColor,
                            startAngle = startAngle,
                            sweepAngle = easyAngle,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                center.x - radius,
                                center.y - radius
                            ),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                        startAngle += easyAngle
                        
                        // Medium arc
                        drawArc(
                            color = mediumColor,
                            startAngle = startAngle,
                            sweepAngle = mediumAngle,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                center.x - radius,
                                center.y - radius
                            ),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                        startAngle += mediumAngle
                        
                        // Hard arc
                        drawArc(
                            color = hardColor,
                            startAngle = startAngle,
                            sweepAngle = hardAngle,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                center.x - radius,
                                center.y - radius
                            ),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                    } else {
                        // Empty state
                        drawArc(
                            color = glassColors.textSecondary.copy(alpha = 0.2f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                center.x - radius,
                                center.y - radius
                            ),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                    }
                }
                
                // Center text
                Text(
                    text = "$total",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.textPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Legend
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LegendItem("Easy", data.easy, easyColor, glassColors)
                LegendItem("Medium", data.medium, mediumColor, glassColors)
                LegendItem("Hard", data.hard, hardColor, glassColors)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, color: Color, glassColors: GlassColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = glassColors.textSecondary
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$count",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = glassColors.textPrimary
        )
    }
}

@Composable
private fun AchievementsCompactCard(
    achievements: List<AchievementData>,
    hazeState: HazeState,
    glassColors: GlassColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedCount = achievements.count { it.unlocked }
    val totalCount = achievements.size.coerceAtLeast(1)
    val percentage = if (totalCount > 0) (unlockedCount.toFloat() / totalCount.toFloat()) else 0f
    
    val backgroundColor = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    
    // Yellow/gold glow that increases with more achievements
    val glowColor = Color(0xFFFFD700) // Gold
    val glowIntensity = percentage.coerceIn(0f, 1f)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor,
                        if (glowIntensity > 0) glowColor.copy(alpha = glowIntensity * 0.4f) else backgroundColor
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Achievements",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = glassColors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            // Centered number - takes up most of the space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$unlockedCount",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = if (glowIntensity > 0.5f) 
                        Color(0xFFFFD700) 
                    else 
                        glassColors.textPrimary,
                    letterSpacing = (-4).sp
                )
            }
            
            // Bottom text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "of $totalCount",
                    fontSize = 14.sp,
                    color = glassColors.textSecondary
                )
                
                Text(
                    text = "unlocked",
                    fontSize = 12.sp,
                    color = glassColors.textSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(data: DifficultyData, hazeState: HazeState, glassColors: GlassColors) {
    val total = data.easy + data.medium + data.hard
    val easyProgress = if (total > 0) data.easy.toFloat() / total else 0f
    val mediumProgress = if (total > 0) data.medium.toFloat() / total else 0f
    val hardProgress = if (total > 0) data.hard.toFloat() / total else 0f
    
    val easyColor = if (glassColors.isDark) Color(0xFFCCCCCC) else Color(0xFF333333)
    val mediumColor = if (glassColors.isDark) Color(0xFF999999) else Color(0xFF666666)
    val hardColor = if (glassColors.isDark) Color(0xFF666666) else Color(0xFF999999)
    
    GlassCardContainer(hazeState = hazeState, glassColors = glassColors) {
        Column {
            CardHeader(title = "Difficulty", icon = Icons.Default.PieChart, glassColors = glassColors)
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                DifficultyBar("Easy", data.easy, easyProgress, easyColor, glassColors)
                DifficultyBar("Medium", data.medium, mediumProgress, mediumColor, glassColors)
                DifficultyBar("Hard", data.hard, hardProgress, hardColor, glassColors)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GlassSubsection(glassColors = glassColors) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DifficultyPill("Easy", if (total > 0) (data.easy * 100 / total) else 0, easyColor)
                    DifficultyPill("Medium", if (total > 0) (data.medium * 100 / total) else 0, mediumColor)
                    DifficultyPill("Hard", if (total > 0) (data.hard * 100 / total) else 0, hardColor)
                }
            }
        }
    }
}

@Composable
private fun DifficultyBar(label: String, count: Int, progress: Float, color: Color, glassColors: GlassColors) {
    val maxHeight = 80.dp
    val barHeight = maxHeight * progress * 2.5f
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Text(
            text = count.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = glassColors.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(barHeight.coerceAtMost(maxHeight))
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.85f))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = glassColors.textPrimary
        )
    }
}

@Composable
private fun DifficultyPill(label: String, percentage: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text("$percentage%", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun PlatformsCard(platforms: List<PlatformData>, hazeState: HazeState, glassColors: GlassColors) {
    val total = platforms.sumOf { it.count }
    
    GlassCardContainer(hazeState = hazeState, glassColors = glassColors) {
        Column {
            CardHeader(title = "Platforms", icon = Icons.Default.Category, glassColors = glassColors)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            platforms.forEach { platform ->
                PlatformRow(platform, total, glassColors)
                if (platform != platforms.last()) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PlatformRow(platform: PlatformData, total: Int, glassColors: GlassColors) {
    val progress = if (total > 0) platform.count.toFloat() / total else 0f
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(platform.color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(platform.name, fontSize = 14.sp, color = glassColors.textPrimary)
            }
            Text("${platform.count} solved", fontSize = 12.sp, color = glassColors.textSecondary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(glassColors.textSecondary.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(platform.color)
            )
        }
    }
}

@Composable
private fun SubmissionStatsCard(stats: SubmissionStats, hazeState: HazeState, glassColors: GlassColors) {
    val acceptedProgress = if (stats.total > 0) stats.accepted.toFloat() / stats.total else 0f
    
    val acceptedColor = glassColors.textPrimary
    val failedColor = glassColors.textSecondary
    
    GlassCardContainer(hazeState = hazeState, glassColors = glassColors) {
        Column {
            CardHeader(title = "Submission Statistics", icon = Icons.Default.Timeline, glassColors = glassColors)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(glassColors.textSecondary.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(acceptedProgress)
                        .height(24.dp)
                        .background(acceptedColor)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassSubsection(glassColors, Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CheckCircle, "Accepted", tint = acceptedColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stats.accepted.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = glassColors.textPrimary)
                        Text("Accepted", fontSize = 11.sp, color = glassColors.textSecondary)
                    }
                }
                GlassSubsection(glassColors, Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Close, "Failed", tint = failedColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stats.failed.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = glassColors.textPrimary)
                        Text("Failed", fontSize = 11.sp, color = glassColors.textSecondary)
                    }
                }
                GlassSubsection(glassColors, Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Timeline, "Total", tint = glassColors.textPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stats.total.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = glassColors.textPrimary)
                        Text("Total", fontSize = 11.sp, color = glassColors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MistakeAnalysisCard(solves: List<Solve>, hazeState: HazeState, glassColors: GlassColors) {
    // Collect all mistake tags from solves
    val mistakeTags = solves.flatMap { solve ->
        solve.submission?.mistakeTags ?: emptyList()
    }.groupingBy { it }.eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(6)
    
    val totalMistakes = mistakeTags.sumOf { it.second }
    val maxCount = mistakeTags.maxOfOrNull { it.second } ?: 1
    
    // Monochrome color for tags
    val tagColor = glassColors.textPrimary
    
    GlassCardContainer(hazeState = hazeState, glassColors = glassColors) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Mistake Analysis",
                    tint = glassColors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mistake Analysis",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.textPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$totalMistakes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.textSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (mistakeTags.isNotEmpty()) {
                // Tags as chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(mistakeTags) { index, (tag, count) ->
                        MistakeTagChip(tag, count, tagColor, glassColors)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bar chart for top mistakes
                GlassSubsection(glassColors = glassColors) {
                    Column {
                        mistakeTags.take(4).forEachIndexed { index, (tag, count) ->
                            MistakeBar(tag, count, maxCount, tagColor, glassColors)
                            if (index < minOf(3, mistakeTags.size - 1)) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No mistakes tracked yet",
                        color = glassColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MistakeTagChip(tag: String, count: Int, color: Color, glassColors: GlassColors) {
    // Dark mode: white bg, black text | Light mode: black bg, white text
    val bgColor = if (glassColors.isDark) Color.White else Color.Black
    val textColor = if (glassColors.isDark) Color.Black else Color.White
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tag, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
private fun MistakeBar(tag: String, count: Int, maxCount: Int, color: Color, glassColors: GlassColors) {
    val progress = if (maxCount > 0) count.toFloat() / maxCount else 0f
    
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = tag,
            fontSize = 12.sp,
            color = glassColors.textSecondary,
            modifier = Modifier.width(80.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(glassColors.textSecondary.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = glassColors.textPrimary,
            modifier = Modifier.width(24.dp)
        )
    }
}

@Composable
private fun ProblemDistributionCard(categories: List<CategoryData>, hazeState: HazeState, glassColors: GlassColors) {
    val maxCount = categories.maxOfOrNull { it.count } ?: 1
    val categoryColors = if (glassColors.isDark) listOf(
        Color(0xFFFFFFFF), Color(0xFFCCCCCC), Color(0xFFAAAAAA),
        Color(0xFF888888), Color(0xFF666666), Color(0xFF444444)
    ) else listOf(
        Color(0xFF000000), Color(0xFF333333), Color(0xFF555555),
        Color(0xFF777777), Color(0xFF999999), Color(0xFFBBBBBB)
    )
    
    GlassCardContainer(hazeState = hazeState, glassColors = glassColors) {
        Column {
            CardHeader(title = "Problem Distribution", icon = Icons.Default.Category, glassColors = glassColors)
            Spacer(modifier = Modifier.height(20.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(categories) { index, category ->
                    CategoryChip(category, categoryColors[index % categoryColors.size], glassColors)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GlassSubsection(glassColors = glassColors) {
                Column {
                    categories.take(4).forEachIndexed { index, category ->
                        CategoryBar(category, maxCount, categoryColors[index % categoryColors.size], glassColors)
                        if (index < 3) Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: CategoryData, color: Color, glassColors: GlassColors) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
            Spacer(modifier = Modifier.width(6.dp))
            Text(category.count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun CategoryBar(category: CategoryData, maxCount: Int, color: Color, glassColors: GlassColors) {
    val progress = if (maxCount > 0) category.count.toFloat() / maxCount else 0f
    
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(category.name, fontSize = 12.sp, color = glassColors.textSecondary, modifier = Modifier.width(60.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(glassColors.textSecondary.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(category.count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glassColors.textPrimary, modifier = Modifier.width(24.dp))
    }
}

// Helper function to format time in seconds to readable string
private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}

@Composable
private fun TimePerformanceChartCard(solves: List<Solve>, hazeState: HazeState, glassColors: GlassColors) {
    // Filter solves with time data
    val timeData = solves.mapNotNull { solve ->
        solve.submission?.timeTaken?.let { time ->
            Pair(solve.problem.title ?: solve.problem.slug, time)
        }
    }.reversed().takeLast(15) // Take last 15 for chart
    
    val avgTime = if (timeData.isNotEmpty()) timeData.map { it.second }.average().toInt() else 0
    val fastestTime = timeData.minOfOrNull { it.second } ?: 0
    
    // Monochrome colors
    val chartColor = glassColors.textPrimary
    val chartColorEnd = glassColors.textSecondary
    
    GlassCardContainer(hazeState = hazeState, glassColors = glassColors) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time Performance",
                    tint = glassColors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Time Performance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.textPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (timeData.isNotEmpty()) {
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = formatTime(avgTime),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = chartColor
                        )
                        Text(
                            text = "Average Time",
                            fontSize = 13.sp,
                            color = glassColors.textSecondary
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatTime(fastestTime),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = chartColorEnd
                        )
                        Text(
                            text = "Fastest",
                            fontSize = 13.sp,
                            color = glassColors.textSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Vico Line Chart
                val modelProducer = remember { CartesianChartModelProducer() }
                
                LaunchedEffect(timeData) {
                    modelProducer.runTransaction {
                        lineSeries { series(timeData.map { it.second }) }
                    }
                }
                
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(
                            lineProvider = LineCartesianLayer.LineProvider.series(
                                LineCartesianLayer.rememberLine(
                                    fill = LineCartesianLayer.LineFill.single(fill(chartColor)),
                                    areaFill = LineCartesianLayer.AreaFill.single(
                                        fill(chartColor.copy(alpha = 0.3f))
                                    )
                                )
                            )
                        )
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No time data available",
                        color = glassColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AttemptsAnalysisCard(solves: List<Solve>, hazeState: HazeState, glassColors: GlassColors) {
    // Filter solves with tries data
    val triesData = solves.mapNotNull { solve ->
        solve.submission?.numberOfTries?.let { tries ->
            if (tries > 0) Triple(solve.problem.title ?: solve.problem.slug, tries, solve.problem.difficulty ?: "unknown")
            else null
        }
    }.reversed().takeLast(20)
    
    val avgTries = if (triesData.isNotEmpty()) triesData.map { it.second.toDouble() }.average() else 0.0
    val firstTryCount = triesData.count { it.second == 1 }
    
    // Colors for difficulty
    val easyColor = Color(0xFFB388FF) // Light purple
    val mediumColor = Color(0xFFFF80AB) // Pink
    val hardColor = Color(0xFF82B1FF) // Light blue
    val accentColor = Color(0xFFB388FF)
    
    GlassCardContainer(hazeState = hazeState, glassColors = glassColors) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Attempts Analysis",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Attempts Analysis",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.textPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (triesData.isNotEmpty()) {
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = String.format("%.1f", avgTries),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = "Average Tries",
                            fontSize = 13.sp,
                            color = glassColors.textSecondary
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$firstTryCount",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = easyColor
                        )
                        Text(
                            text = "First Try",
                            fontSize = 13.sp,
                            color = glassColors.textSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Scatter-like Point Chart using Canvas (Vico doesn't have native scatter)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxTries = triesData.maxOfOrNull { it.second } ?: 1
                        val pointSpacing = size.width / (triesData.size + 1)
                        
                        triesData.forEachIndexed { index, (_, tries, difficulty) ->
                            val x = pointSpacing * (index + 1)
                            val y = size.height - (tries.toFloat() / maxTries.toFloat() * size.height * 0.8f) - size.height * 0.1f
                            
                            val color = when (difficulty.lowercase()) {
                                "easy" -> easyColor
                                "medium" -> mediumColor
                                "hard" -> hardColor
                                else -> Color.Gray
                            }
                            
                            val radius = if (tries == 1) 8.dp.toPx() else 5.dp.toPx()
                            
                            drawCircle(
                                color = color,
                                radius = radius,
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(easyColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Easy", fontSize = 11.sp, color = glassColors.textSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(mediumColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Medium", fontSize = 11.sp, color = glassColors.textSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(hardColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hard", fontSize = 11.sp, color = glassColors.textSecondary)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No attempts data available",
                        color = glassColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSolvesCard(
    solves: List<RecentSolve>, 
    hazeState: HazeState, 
    glassColors: GlassColors,
    onClick: () -> Unit = {}
) {
    GlassCardContainer(
        hazeState = hazeState, 
        glassColors = glassColors,
        modifier = Modifier.clickable { onClick() }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardHeader(title = "Recent Solves", icon = Icons.Default.History, glassColors = glassColors)
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "View All",
                    tint = glassColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = glassColors.textSecondary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            solves.forEachIndexed { index, solve ->
                RecentSolveItem(solve, glassColors)
                if (index < solves.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RecentSolveItem(solve: RecentSolve, glassColors: GlassColors) {
    val difficultyColor = when (solve.difficulty.lowercase()) {
        "easy" -> Color(0xFF22C55E)
        "medium" -> Color(0xFFFBBF24)
        "hard" -> Color(0xFFEF4444)
        else -> glassColors.textSecondary
    }
    
    GlassSubsection(glassColors = glassColors) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(solve.problemName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = glassColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(solve.platform, fontSize = 11.sp, color = glassColors.textSecondary)
                    Text(" · ", fontSize = 11.sp, color = glassColors.textSecondary)
                    Text(solve.difficulty, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = difficultyColor)
                }
            }
            Text(solve.timeAgo, fontSize = 11.sp, color = glassColors.textSecondary)
        }
    }
}

@Composable
private fun AchievementsSection(
    achievements: List<AchievementData>,
    hazeState: HazeState,
    glassColors: GlassColors,
    onClick: () -> Unit = {}
) {
    val unlockedCount = achievements.count { it.unlocked }
    val totalCount = achievements.size
    val percentage = if (totalCount > 0) (unlockedCount.toFloat() / totalCount.toFloat()) else 0f
    
    GlassCardContainer(
        hazeState = hazeState, 
        glassColors = glassColors, 
        modifier = Modifier.clickable(enabled = true) { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE91E8C),
                                    Color(0xFFA855F7)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Achievements",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "Achievements",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$unlockedCount of $totalCount unlocked",
                        fontSize = 12.sp,
                        color = glassColors.textSecondary
                    )
                }
            }
            
            // Right side - Circular Progress
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = percentage,
                    modifier = Modifier.size(64.dp),
                    color = if (glassColors.isDark) Color(0xFFE91E8C) else Color(0xFFA855F7),
                    strokeWidth = 6.dp,
                    trackColor = if (glassColors.isDark) Color(0x30FFFFFF) else Color(0x30E91E8C)
                )
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (glassColors.isDark) Color.White else Color(0xFFE91E8C)
                )
            }
        }
    }
}

@Composable
private fun EmptyAchievementsSection(
    hazeState: HazeState,
    glassColors: GlassColors,
    onClick: () -> Unit = {}
) {
    GlassCardContainer(
        hazeState = hazeState, 
        glassColors = glassColors, 
        modifier = Modifier.clickable(enabled = true) { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE91E8C),
                                    Color(0xFFA855F7)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Achievements",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "Achievements",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start solving to unlock",
                        fontSize = 12.sp,
                        color = glassColors.textSecondary
                    )
                }
            }
            
            // Right side - Lock icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (glassColors.isDark) Color(0x20FFFFFF) 
                        else Color(0x20E91E8C)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = glassColors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AchievementItem(achievement: AchievementData, glassColors: GlassColors, modifier: Modifier) {
    if (achievement.name.isEmpty()) {
        return
    }
    
    val bgColor = if (achievement.unlocked) {
        if (glassColors.isDark) Color(0x30FFFFFF) else Color(0x50E91E8C)
    } else {
        if (glassColors.isDark) Color(0x10FFFFFF) else Color(0x20000000)
    }
    
    val borderColor = if (achievement.unlocked) {
        if (glassColors.isDark) Color.White.copy(alpha = 0.3f) else Color(0xFFE91E8C).copy(alpha = 0.5f)
    } else {
        if (glassColors.isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
    }
    
    Box(
        modifier = modifier
            .height(90.dp)  // Fixed height for uniform sizing
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .alpha(if (achievement.unlocked) 1f else 0.5f)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = if (achievement.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                contentDescription = achievement.name,
                tint = if (achievement.unlocked) {
                    if (glassColors.isDark) Color.White else Color(0xFFE91E8C)
                } else {
                    glassColors.textSecondary
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = achievement.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = glassColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun ProblemsSummaryCard(
    completed: Int,
    total: Int,
    hazeState: HazeState,
    glassColors: GlassColors,
    onClick: () -> Unit
) {
    val remaining = total - completed
    
    // Dark mode: white bg, black text | Light mode: black bg, white text
    val cardBg = if (glassColors.isDark) Color.White else Color.Black
    val textColor = if (glassColors.isDark) Color.Black else Color.White
    val secondaryTextColor = if (glassColors.isDark) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Problems",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Solved",
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$completed solved",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "$remaining remaining",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondaryTextColor
                    )
                }
            }
            
            // Arrow indicator
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = "View Problems",
                tint = secondaryTextColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
