package com.example.traverse2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traverse2.data.api.FriendAchievementItem
import com.example.traverse2.data.api.FriendItem
import com.example.traverse2.data.api.FriendSolveItem
import com.example.traverse2.data.api.FriendStatsResponse
import com.example.traverse2.data.api.FriendStreakItem
import com.example.traverse2.data.api.ReceivedFriendRequest
import com.example.traverse2.data.api.ReceivedStreakRequest
import com.example.traverse2.data.api.SentFriendRequest
import com.example.traverse2.data.api.SentStreakRequest
import com.example.traverse2.data.api.UserSearchResult
import com.example.traverse2.data.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Main UI State for Friends screen
data class FriendsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val friends: List<FriendItem> = emptyList(),
    val receivedRequests: List<ReceivedFriendRequest> = emptyList(),
    val sentRequests: List<SentFriendRequest> = emptyList(),
    val friendStreaks: List<FriendStreakItem> = emptyList(),
    val receivedStreakRequests: List<ReceivedStreakRequest> = emptyList(),
    val sentStreakRequests: List<SentStreakRequest> = emptyList(),
    // Search
    val searchQuery: String = "",
    val searchResults: List<UserSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    // Action states
    val actionInProgress: Boolean = false,
    val actionMessage: String? = null
)

// Friend Profile State
data class FriendProfileState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val username: String = "",
    val stats: FriendStatsResponse? = null,
    val solves: List<FriendSolveItem> = emptyList(),
    val achievements: List<FriendAchievementItem> = emptyList()
)

class FriendsViewModel : ViewModel() {

    private val repository = FriendsRepository()

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val _friendProfileState = MutableStateFlow(FriendProfileState())
    val friendProfileState: StateFlow<FriendProfileState> = _friendProfileState.asStateFlow()

    init {
        loadFriendsData()
    }

    fun loadFriendsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load friends list
                val friendsResult = repository.getFriends()
                val friends = friendsResult.getOrDefault(emptyList())

                // Load friend requests
                val receivedResult = repository.getReceivedFriendRequests()
                val received = receivedResult.getOrDefault(emptyList())

                val sentResult = repository.getSentFriendRequests()
                val sent = sentResult.getOrDefault(emptyList())

                // Load friend streaks
                val streaksResult = repository.getFriendStreaks()
                val streaks = streaksResult.getOrDefault(emptyList())

                // Load streak requests
                val receivedStreakResult = repository.getReceivedStreakRequests()
                val receivedStreakRequests = receivedStreakResult.getOrDefault(emptyList())

                val sentStreakResult = repository.getSentStreakRequests()
                val sentStreakRequests = sentStreakResult.getOrDefault(emptyList())

                _uiState.value = FriendsUiState(
                    isLoading = false,
                    friends = friends,
                    receivedRequests = received,
                    sentRequests = sent,
                    friendStreaks = streaks,
                    receivedStreakRequests = receivedStreakRequests,
                    sentStreakRequests = sentStreakRequests
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load friends data"
                )
            }
        }
    }

    // ========== FRIEND REQUESTS ==========

    fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionMessage = null)

            val result = repository.sendFriendRequest(username)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Friend request sent to $username"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to send request"
                )
            }
        }
    }

    fun acceptFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true)

            val result = repository.acceptFriendRequest(requestId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Friend request accepted"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun rejectFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true)

            val result = repository.rejectFriendRequest(requestId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Friend request rejected"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun cancelFriendRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true)

            val result = repository.cancelFriendRequest(requestId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Friend request cancelled"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun removeFriend(username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true)

            val result = repository.removeFriend(username)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Removed $username from friends"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // ========== FRIEND STREAKS ==========

    fun sendStreakRequest(username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionMessage = null)

            val result = repository.sendStreakRequest(username)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Streak request sent to $username"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to send streak request"
                )
            }
        }
    }

    fun acceptStreakRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true)

            val result = repository.acceptStreakRequest(requestId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Streak request accepted"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun rejectStreakRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true)

            val result = repository.rejectStreakRequest(requestId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Streak request rejected"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun cancelStreakRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true)

            val result = repository.cancelStreakRequest(requestId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Streak request cancelled"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun deleteFriendStreak(username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true)

            val result = repository.deleteFriendStreak(username)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    actionMessage = "Streak with $username ended"
                )
                loadFriendsData()
            } else {
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // ========== USER SEARCH ==========

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length >= 2) {
            searchUsers(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)

            val result = repository.searchUsers(query)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    searchResults = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    searchResults = emptyList()
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    // ========== FRIEND PROFILE ==========

    fun loadFriendProfile(username: String) {
        viewModelScope.launch {
            _friendProfileState.value = FriendProfileState(isLoading = true, username = username)

            try {
                // Load stats
                val statsResult = repository.getFriendStats(username)
                val stats = statsResult.getOrNull()

                // Load recent solves
                val solvesResult = repository.getFriendSolves(username, limit = 10)
                val solves = solvesResult.getOrDefault(
                    com.example.traverse2.data.api.FriendSolvesResponse(emptyList(),
                        com.example.traverse2.data.api.PaginationInfo(0, 10, 0))
                ).solves

                // Load achievements
                val achievementsResult = repository.getFriendAchievements(username)
                val achievements = achievementsResult.getOrDefault(
                    com.example.traverse2.data.api.FriendAchievementsResponse(emptyList())
                ).achievements

                _friendProfileState.value = FriendProfileState(
                    isLoading = false,
                    username = username,
                    stats = stats,
                    solves = solves,
                    achievements = achievements
                )
            } catch (e: Exception) {
                _friendProfileState.value = _friendProfileState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load friend profile"
                )
            }
        }
    }

    // ========== UTILITY ==========

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }

    fun refresh() {
        loadFriendsData()
    }
}
