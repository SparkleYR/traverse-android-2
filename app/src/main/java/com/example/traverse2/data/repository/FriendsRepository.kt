package com.example.traverse2.data.repository

import com.example.traverse2.data.api.AcceptFriendResponse
import com.example.traverse2.data.api.AcceptStreakResponse
import com.example.traverse2.data.api.FriendAchievementsResponse
import com.example.traverse2.data.api.FriendItem
import com.example.traverse2.data.api.FriendRequestBody
import com.example.traverse2.data.api.FriendRequestResponse
import com.example.traverse2.data.api.FriendSolvesResponse
import com.example.traverse2.data.api.FriendStatsResponse
import com.example.traverse2.data.api.FriendStreakItem
import com.example.traverse2.data.api.FriendStreakRequestBody
import com.example.traverse2.data.api.FriendStreakRequestResponse
import com.example.traverse2.data.api.ReceivedFriendRequest
import com.example.traverse2.data.api.ReceivedStreakRequest
import com.example.traverse2.data.api.RetrofitClient
import com.example.traverse2.data.api.SentFriendRequest
import com.example.traverse2.data.api.SentStreakRequest
import com.example.traverse2.data.api.UserSearchResult

class FriendsRepository {
    private val api = RetrofitClient.api

    // ========== FRIENDS ==========

    suspend fun getFriends(): Result<List<FriendItem>> {
        return try {
            val response = api.getFriends()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.friends)
            } else {
                Result.failure(Exception("Failed to get friends: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(username: String): Result<FriendRequestResponse> {
        return try {
            val response = api.sendFriendRequest(FriendRequestBody(username))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Failed to send friend request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceivedFriendRequests(): Result<List<ReceivedFriendRequest>> {
        return try {
            val response = api.getReceivedFriendRequests()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get received requests: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSentFriendRequests(): Result<List<SentFriendRequest>> {
        return try {
            val response = api.getSentFriendRequests()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get sent requests: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(requestId: Int): Result<AcceptFriendResponse> {
        return try {
            val response = api.acceptFriendRequest(requestId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to accept request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectFriendRequest(requestId: Int): Result<Unit> {
        return try {
            val response = api.rejectFriendRequest(requestId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to reject request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelFriendRequest(requestId: Int): Result<Unit> {
        return try {
            val response = api.cancelFriendRequest(requestId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to cancel request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(username: String): Result<Unit> {
        return try {
            val response = api.removeFriend(username)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove friend: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== FRIEND PROFILE ==========

    suspend fun getFriendSolves(username: String, limit: Int = 50, offset: Int = 0): Result<FriendSolvesResponse> {
        return try {
            val response = api.getFriendSolves(username, limit, offset)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get friend solves: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendStats(username: String): Result<FriendStatsResponse> {
        return try {
            val response = api.getFriendStats(username)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get friend stats: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriendAchievements(username: String): Result<FriendAchievementsResponse> {
        return try {
            val response = api.getFriendAchievements(username)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get friend achievements: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== FRIEND STREAKS ==========

    suspend fun getFriendStreaks(): Result<List<FriendStreakItem>> {
        return try {
            val response = api.getFriendStreaks()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get friend streaks: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendStreakRequest(username: String): Result<FriendStreakRequestResponse> {
        return try {
            val response = api.sendFriendStreakRequest(FriendStreakRequestBody(username))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Failed to send streak request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceivedStreakRequests(): Result<List<ReceivedStreakRequest>> {
        return try {
            val response = api.getReceivedStreakRequests()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get received streak requests: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSentStreakRequests(): Result<List<SentStreakRequest>> {
        return try {
            val response = api.getSentStreakRequests()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get sent streak requests: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptStreakRequest(requestId: Int): Result<AcceptStreakResponse> {
        return try {
            val response = api.acceptStreakRequest(requestId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to accept streak request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectStreakRequest(requestId: Int): Result<Unit> {
        return try {
            val response = api.rejectStreakRequest(requestId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to reject streak request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelStreakRequest(requestId: Int): Result<Unit> {
        return try {
            val response = api.cancelStreakRequest(requestId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to cancel streak request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFriendStreak(username: String): Result<Unit> {
        return try {
            val response = api.deleteFriendStreak(username)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete friend streak: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== USER SEARCH ==========

    suspend fun searchUsers(query: String, limit: Int = 10): Result<List<UserSearchResult>> {
        return try {
            val response = api.searchUsers(query, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.users)
            } else {
                Result.failure(Exception("Failed to search users: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
