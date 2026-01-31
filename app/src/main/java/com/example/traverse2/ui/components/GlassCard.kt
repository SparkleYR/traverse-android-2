package com.example.traverse2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.traverse2.ui.theme.TraverseTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = TraverseTheme.glassColors
    
    val backgroundColor = if (glassColors.isDark) 
        Color(0xFF1C1C1E)
    else 
        Color(0xFFF2F2F7)
    
    val borderColor = if (glassColors.isDark) 
        Color.White.copy(alpha = 0.1f) 
    else 
        Color.Black.copy(alpha = 0.1f)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun SimpleGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = TraverseTheme.glassColors
    
    val backgroundColor = if (glassColors.isDark) 
        Color(0xFF1C1C1E)
    else 
        Color(0xFFF2F2F7)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(16.dp),
        content = content
    )
}
