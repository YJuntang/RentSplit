package com.rentsplit.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.preferences.UserPreferencesRepository
import com.rentsplit.data.repository.RentSplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class OnboardingUiState(
    val householdName: String = "",
    val userName: String = "",
    val housemates: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val uiError: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: RentSplitRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateHouseholdName(name: String) {
        _uiState.update { it.copy(householdName = name) }
    }

    fun updateUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun addHousemate() {
        _uiState.update { it.copy(housemates = it.housemates + "") }
    }

    fun updateHousemate(index: Int, name: String) {
        _uiState.update { state ->
            val updatedList = state.housemates.toMutableList()
            if (index in updatedList.indices) {
                updatedList[index] = name
            }
            state.copy(housemates = updatedList)
        }
    }

    fun removeHousemate(index: Int) {
        _uiState.update { state ->
            val updatedList = state.housemates.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
            }
            state.copy(housemates = updatedList)
        }
    }

    fun completeOnboarding() {
        val state = _uiState.value
        if (state.householdName.isBlank() || state.userName.isBlank()) {
            _uiState.update { it.copy(uiError = "Household and your name are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, uiError = null) }
            try {
                // 1. Create Household
                val householdId = repository.insertHousehold(Household(name = state.householdName.trim()))

                // 2. Add Primary User
                repository.insertMember(
                    Member(
                        householdId = householdId,
                        name = state.userName.trim(),
                        colorHex = getRandomColor()
                    )
                )

                // 3. Add Housemates
                state.housemates.filter { it.isNotBlank() }.forEach { housemateName ->
                    repository.insertMember(
                        Member(
                            householdId = householdId,
                            name = housemateName.trim(),
                            colorHex = getRandomColor()
                        )
                    )
                }

                // 4. Update Preferences
                preferencesRepository.setHasCompletedOnboarding(true)

                _uiState.update { it.copy(isSaving = false, isComplete = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, uiError = e.message ?: "Failed to save onboarding data") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(uiError = null) }
    }

    private fun getRandomColor(): String {
        // Simple vibrant colors based on predefined hexes for consistency if needed,
        // or just random hex. We'll use a fixed set of good looking colors.
        val colors = listOf(
            "#EF4444", "#F97316", "#F59E0B", "#10B981",
            "#06B6D4", "#3B82F6", "#6366F1", "#8B5CF6",
            "#D946EF", "#F43F5E"
        )
        return colors.random()
    }
}
