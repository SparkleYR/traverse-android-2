package com.example.traverse2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.traverse2.ui.theme.TraverseTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    content: @Composable () -> Unit
) {
    val glassColors = TraverseTheme.glassColors
    
    // Simple solid background with no blur or gradients
    val backgroundColor = if (glassColors.isDark) 
        Color(0xFF000000)
    else 
        Color(0xFFFFFFFF)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        content()
    }
}
