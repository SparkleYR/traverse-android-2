package com.example.traverse2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.traverse2.data.api.FriendItem
import com.example.traverse2.ui.theme.GlassColors
import com.example.traverse2.ui.theme.TraverseTheme
import com.example.traverse2.ui.viewmodel.FreezeShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreezeShopScreen(
    onBack: () -> Unit,
    viewModel: FreezeShopViewModel = viewModel()
) {
    val glassColors = TraverseTheme.glassColors
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedCount by remember { mutableIntStateOf(1) }
    var showingInfo by remember { mutableStateOf(false) }
    var showGiftDialog by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<FriendItem?>(null) }
    
    val freezeCost = uiState.freezeInfo?.costs?.purchase ?: 100
    val totalCost = selectedCount * freezeCost
    val canAfford = uiState.userXp >= totalCost
    
    val snowflakeColor = Color(0xFF00BCD4) // Cyan - only for snowflake icon
    val accentColor = Color(0xFFFFFFFF) // White for selections
    val buttonColor = Color(0xFF3A3A3C) // Dark gray for buttons
    val backgroundColor = if (glassColors.isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBackground = if (glassColors.isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val secondaryCardBg = if (glassColors.isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
    
    // Show snackbar for success/error messages
    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Freeze Shop", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = accentColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Freeze Status Card
                FreezeStatusCard(
                    availableFreezes = uiState.freezeInfo?.availableFreezes ?: 0,
                    usedFreezes = uiState.freezeInfo?.usedFreezes ?: 0,
                    userXp = uiState.userXp,
                    snowflakeColor = snowflakeColor,
                    cardBackground = cardBackground,
                    glassColors = glassColors
                )
                
                // Purchase Section
                PurchaseSection(
                    selectedCount = selectedCount,
                    onCountSelected = { selectedCount = it },
                    totalCost = totalCost,
                    canAfford = canAfford,
                    isPurchasing = uiState.isPurchasing,
                    userXp = uiState.userXp,
                    showingInfo = showingInfo,
                    onToggleInfo = { showingInfo = !showingInfo },
                    onPurchase = { viewModel.purchaseFreeze(selectedCount) },
                    accentColor = accentColor,
                    buttonColor = buttonColor,
                    snowflakeColor = snowflakeColor,
                    cardBackground = secondaryCardBg,
                    glassColors = glassColors
                )
                
                // Gift Section
                if (uiState.friends.isNotEmpty()) {
                    GiftSection(
                        friends = uiState.friends,
                        giftCost = uiState.freezeInfo?.costs?.gift ?: 70,
                        userXp = uiState.userXp,
                        isGifting = uiState.isGifting,
                        onGift = { friend ->
                            selectedFriend = friend
                            showGiftDialog = true
                        },
                        buttonColor = buttonColor,
                        cardBackground = secondaryCardBg,
                        glassColors = glassColors
                    )
                }
            }
        }
    }
    
    // Gift Confirmation Dialog
    if (showGiftDialog && selectedFriend != null) {
        val giftCost = uiState.freezeInfo?.costs?.gift ?: 70
        AlertDialog(
            onDismissRequest = { showGiftDialog = false },
            title = { Text("Gift Freeze") },
            text = { 
                Text("Send a freeze to ${selectedFriend!!.username} for $giftCost XP?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.giftFreeze(selectedFriend!!.username, 1)
                        showGiftDialog = false
                        selectedFriend = null
                    },
                    enabled = uiState.userXp >= giftCost
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGiftDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FreezeStatusCard(
    availableFreezes: Int,
    usedFreezes: Int,
    userXp: Int,
    snowflakeColor: Color,
    cardBackground: Color,
    glassColors: GlassColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = null,
                    tint = snowflakeColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$availableFreezes",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary
                    )
                    Text(
                        text = "Available Freezes",
                        fontSize = 14.sp,
                        color = glassColors.textSecondary
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$userXp",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.accent
                )
                Text(
                    text = "XP Balance",
                    fontSize = 12.sp,
                    color = glassColors.textSecondary
                )
            }
        }
        
        if (usedFreezes > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$usedFreezes freeze${if (usedFreezes == 1) "" else "s"} used to save your streak",
                    fontSize = 12.sp,
                    color = glassColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PurchaseSection(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit,
    totalCost: Int,
    canAfford: Boolean,
    isPurchasing: Boolean,
    userXp: Int,
    showingInfo: Boolean,
    onToggleInfo: () -> Unit,
    onPurchase: () -> Unit,
    accentColor: Color,
    buttonColor: Color,
    snowflakeColor: Color,
    cardBackground: Color,
    glassColors: GlassColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Purchase Freezes",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = glassColors.textPrimary
            )
            
            IconButton(onClick = onToggleInfo) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = snowflakeColor
                )
            }
        }
        
        // Expandable Info Section
        AnimatedVisibility(
            visible = showingInfo,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (glassColors.isDark) Color(0xFF3C3C3E) else Color(0xFFE5E5EA))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FreezeInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    text = "Freezes are automatically used when you miss a day",
                    iconColor = glassColors.textSecondary,
                    textColor = glassColors.textSecondary
                )
                FreezeInfoRow(
                    icon = Icons.Default.LocalFireDepartment,
                    text = "Your streak is preserved instead of resetting to 0",
                    iconColor = glassColors.textSecondary,
                    textColor = glassColors.textSecondary
                )
                FreezeInfoRow(
                    icon = Icons.Default.CardGiftcard,
                    text = "Gift freezes to friends for 70 XP each",
                    iconColor = glassColors.textSecondary,
                    textColor = glassColors.textSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quantity Selector - Circle Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..5).forEach { count ->
                QuantityCircleButton(
                    count = count,
                    isSelected = selectedCount == count,
                    onClick = { onCountSelected(count) },
                    accentColor = accentColor,
                    glassColors = glassColors
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Purchase Button
        Button(
            onClick = onPurchase,
            enabled = canAfford && !isPurchasing,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF2C2C2E),
                disabledContentColor = Color(0xFF8E8E93)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isPurchasing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = null,
                    tint = snowflakeColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Purchase for $totalCost XP",
                    fontWeight = FontWeight.SemiBold,
                    color = if (canAfford) Color.White else Color(0xFF8E8E93)
                )
            }
        }
        
        if (!canAfford) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Not enough XP. You need ${totalCost - userXp} more XP.",
                fontSize = 12.sp,
                color = Color(0xFFFF9800),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuantityCircleButton(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    glassColors: GlassColors
) {
    val backgroundColor = if (isSelected) {
        accentColor.copy(alpha = 0.2f)
    } else {
        if (glassColors.isDark) Color(0xFF3C3C3E) else Color(0xFFE5E5EA)
    }
    
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, accentColor, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) accentColor else glassColors.textPrimary
        )
    }
}

@Composable
private fun FreezeInfoRow(
    icon: ImageVector,
    text: String,
    iconColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GiftSection(
    friends: List<FriendItem>,
    giftCost: Int,
    userXp: Int,
    isGifting: Boolean,
    onGift: (FriendItem) -> Unit,
    buttonColor: Color,
    cardBackground: Color,
    glassColors: GlassColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                tint = glassColors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Gift to Friends",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = glassColors.textPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "$giftCost XP per freeze",
            fontSize = 12.sp,
            color = glassColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        friends.forEach { friend ->
            GiftFriendRow(
                friend = friend,
                canAfford = userXp >= giftCost,
                isGifting = isGifting,
                onGift = { onGift(friend) },
                buttonColor = buttonColor,
                glassColors = glassColors
            )
            if (friend != friends.last()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GiftFriendRow(
    friend: FriendItem,
    canAfford: Boolean,
    isGifting: Boolean,
    onGift: () -> Unit,
    buttonColor: Color,
    glassColors: GlassColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (glassColors.isDark) Color(0xFF3C3C3E) else Color(0xFFE5E5EA))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = friend.username,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = glassColors.textPrimary
            )
        }
        
        Button(
            onClick = onGift,
            enabled = canAfford && !isGifting,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF2C2C2E),
                disabledContentColor = Color(0xFF8E8E93)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Gift", fontSize = 13.sp)
        }
    }
}
