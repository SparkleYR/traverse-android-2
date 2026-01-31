package com.example.traverse2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traverse2.ui.theme.GlassColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

@Composable
fun GlassTopBar(
    title: String,
    hazeState: HazeState,
    glassColors: GlassColors,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color? = null
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val topPadding = statusBarPadding.calculateTopPadding()
    
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Solid background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topPadding + 70.dp)
                .background(
                    if (glassColors.isDark) 
                        Color(0xFF1C1C1E)
                    else 
                        Color(0xFFF2F2F7)
                )
        )
        
        // Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding)
                .height(60.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = glassColors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint ?: glassColors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = glassColors.textPrimary
            )
        }
    }
}
