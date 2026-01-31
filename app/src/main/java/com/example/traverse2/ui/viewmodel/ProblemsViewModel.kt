package com.example.traverse2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traverse2.data.api.RetrofitClient
import com.example.traverse2.data.api.Solve
import com.example.traverse2.data.api.SolveStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.async

data class ProblemsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val solves: List<Solve> = emptyList(),
    val solveStats: SolveStats? = null,
    val totalProblems: Int = 0
)

class ProblemsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProblemsUiState())
    val uiState: StateFlow<ProblemsUiState> = _uiState.asStateFlow()
    
    init {
        loadProblems()
    }
    
    fun loadProblems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                supervisorScope {
                    val solvesDeferred = async { RetrofitClient.api.getMySolves(limit = 200, offset = 0) }
                    val statsDeferred = async { RetrofitClient.api.getSolveStats() }
                    
                    val solvesResponse = solvesDeferred.await()
                    val statsResponse = statsDeferred.await()
                    
                    if (!solvesResponse.isSuccessful) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load problems"
                        )
                        return@supervisorScope
                    }
                    
                    val solves = solvesResponse.body()?.solves ?: emptyList()
                    val stats = statsResponse.body()?.stats
                    val totalProblems = solvesResponse.body()?.pagination?.total ?: solves.size
                    
                    _uiState.value = ProblemsUiState(
                        isLoading = false,
                        error = null,
                        solves = solves,
                        solveStats = stats,
                        totalProblems = totalProblems
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun refresh() {
        loadProblems()
    }
}
