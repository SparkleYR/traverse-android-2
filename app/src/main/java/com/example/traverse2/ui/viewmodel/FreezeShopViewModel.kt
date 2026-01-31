package com.example.traverse2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traverse2.data.api.FreezeGiftRequest
import com.example.traverse2.data.api.FreezeInfoResponse
import com.example.traverse2.data.api.FreezePurchaseRequest
import com.example.traverse2.data.api.RetrofitClient
import com.example.traverse2.data.api.FriendItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FreezeShopUiState(
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val isGifting: Boolean = false,
    val freezeInfo: FreezeInfoResponse? = null,
    val userXp: Int = 0,
    val friends: List<FriendItem> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class FreezeShopViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FreezeShopUiState())
    val uiState: StateFlow<FreezeShopUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load freeze info
                val freezeResponse = RetrofitClient.api.getFreezeInfo()
                if (freezeResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        freezeInfo = freezeResponse.body()
                    )
                }
                
                // Load user XP
                val userResponse = RetrofitClient.api.getCurrentUser()
                if (userResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        userXp = userResponse.body()?.user?.totalXp ?: 0
                    )
                }
                
                // Load friends for gifting
                val friendsResponse = RetrofitClient.api.getFriends()
                if (friendsResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        friends = friendsResponse.body()?.friends ?: emptyList()
                    )
                }
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load freeze info"
                )
            }
        }
    }
    
    fun purchaseFreeze(count: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true, error = null, successMessage = null)
            
            try {
                val response = RetrofitClient.api.purchaseFreezes(FreezePurchaseRequest(count))
                
                if (response.isSuccessful) {
                    val body = response.body()
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        successMessage = body?.message ?: "Purchase successful!",
                        userXp = body?.remainingXp ?: _uiState.value.userXp
                    )
                    // Reload freeze info
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        error = "Purchase failed: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPurchasing = false,
                    error = e.message ?: "Purchase failed"
                )
            }
        }
    }
    
    fun giftFreeze(toUsername: String, count: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGifting = true, error = null, successMessage = null)
            
            try {
                val response = RetrofitClient.api.giftFreeze(toUsername, FreezeGiftRequest(count))
                
                if (response.isSuccessful) {
                    val body = response.body()
                    _uiState.value = _uiState.value.copy(
                        isGifting = false,
                        successMessage = body?.message ?: "Gift sent!",
                        userXp = body?.remainingXp ?: _uiState.value.userXp
                    )
                    // Reload freeze info
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isGifting = false,
                        error = "Gift failed: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGifting = false,
                    error = e.message ?: "Gift failed"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
