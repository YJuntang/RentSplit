package com.rentsplit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentsplit.data.model.*
import com.rentsplit.data.repository.RentSplitRepository
import com.rentsplit.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import android.content.Context
import android.net.Uri
import com.rentsplit.util.OcrScanner
import com.rentsplit.util.SnackbarManager

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val repository: RentSplitRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Get default split type from preferences
                val prefs = preferencesRepository.userPreferencesFlow.first()
                val defaultSplitType = if (prefs.defaultSplitType == "CUSTOM") SplitType.CUSTOM else SplitType.EQUAL
                
                _uiState.update { it.copy(splitType = defaultSplitType) }

                // Get the first household's members
                val household = repository.getAllHouseholds().first().firstOrNull()
                household?.let { h ->
                    // Collect categories reactively
                    launch {
                        repository.getCategoriesByHousehold(h.id).collectLatest { cats ->
                            _uiState.update { state ->
                                state.copy(
                                    categories = cats,
                                    categoryId = null
                                )
                            }
                        }
                    }

                    repository.getMembersByHousehold(h.id).collectLatest { members ->
                        _uiState.update { state ->
                            state.copy(
                                members = members,
                                paidByMemberId = state.paidByMemberId ?: members.firstOrNull()?.id,
                                memberSplits = members.map { MemberSplitState(it) }
                            )
                        }
                        if (_uiState.value.splitType == SplitType.EQUAL) {
                            updateEqualSplits()
                        } else {
                            updateCustomSplits(_uiState.value.memberSplits)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(uiError = "Failed to load initial data") }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onAmountChange(newAmount: String) {
        _uiState.update { it.copy(amount = newAmount) }
        if (_uiState.value.splitType == SplitType.EQUAL) {
            updateEqualSplits()
        } else if (_uiState.value.splitType == SplitType.CUSTOM) {
            updateCustomSplits(_uiState.value.memberSplits)
        }
    }

    fun onCategoryChange(categoryId: Long) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun onDateChange(newDate: LocalDate) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun onPaidByChange(memberId: Long?) {
        val currentType = _uiState.value.splitType
        _uiState.update { it.copy(paidByMemberId = memberId) }
        
        if (memberId != null && currentType != SplitType.CUSTOM) {
            _uiState.update { it.copy(splitType = SplitType.CUSTOM) }
            val resetSplits = _uiState.value.memberSplits.map {
                it.copy(isManuallyEdited = false)
            }
            updateCustomSplits(resetSplits)
        } else if (currentType == SplitType.EQUAL) {
            updateEqualSplits()
        } else if (currentType == SplitType.CUSTOM) {
            updateCustomSplits(_uiState.value.memberSplits)
        }
    }

    fun onSplitTypeChange(newType: SplitType) {
        _uiState.update { it.copy(splitType = newType) }
        if (newType == SplitType.EQUAL) {
            updateEqualSplits()
        } else if (newType == SplitType.CUSTOM) {
            val resetSplits = _uiState.value.memberSplits.map {
                it.copy(isManuallyEdited = false)
            }
            updateCustomSplits(resetSplits)
        }
    }

    fun onMemberSplitAmountChange(memberId: Long, newAmount: String) {
        _uiState.update { state ->
            val updatedSplits = state.memberSplits.map {
                if (it.member.id == memberId) {
                    it.copy(
                        amount = newAmount,
                        isManuallyEdited = true
                    )
                } else {
                    it
                }
            }
            state.copy(memberSplits = updatedSplits)
        }
        if (_uiState.value.splitType == SplitType.CUSTOM) {
            updateCustomSplits(_uiState.value.memberSplits)
        }
    }

    fun resetMemberSplit(memberId: Long) {
        _uiState.update { state ->
            val updatedSplits = state.memberSplits.map {
                if (it.member.id == memberId) {
                    it.copy(isManuallyEdited = false)
                } else {
                    it
                }
            }
            state.copy(memberSplits = updatedSplits)
        }
        if (_uiState.value.splitType == SplitType.CUSTOM) {
            updateCustomSplits(_uiState.value.memberSplits)
        }
    }

    private fun updateEqualSplits() {
        val totalAmount = _uiState.value.amount.toDoubleOrNull() ?: 0.0
        val membersCount = _uiState.value.members.size
        if (membersCount > 0) {
            val equalShare = Math.round((totalAmount / membersCount) * 100.0) / 100.0
            val remainder = Math.round((totalAmount - equalShare * membersCount) * 100.0) / 100.0
            
            val paidByMemberId = _uiState.value.paidByMemberId
            
            _uiState.update { state ->
                val newSplits = state.memberSplits.map { it.copy(amount = "%.2f".format(equalShare)) }.toMutableList()
                val targetIndex = newSplits.indexOfFirst { it.member.id == paidByMemberId }.takeIf { it != -1 } ?: 0
                
                if (newSplits.isNotEmpty() && remainder != 0.0) {
                    val currentAmount = newSplits[targetIndex].amount.toDoubleOrNull() ?: 0.0
                    newSplits[targetIndex] = newSplits[targetIndex].copy(amount = "%.2f".format(currentAmount + remainder))
                }
                
                state.copy(memberSplits = newSplits)
            }
        }
    }

    private fun updateCustomSplits(updatedSplits: List<MemberSplitState>) {
        val totalAmount = _uiState.value.amount.toDoubleOrNull() ?: 0.0
        
        val editedSplits = updatedSplits.filter { it.isManuallyEdited }
        val editedSum = editedSplits.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        
        val uneditedSplits = updatedSplits.filter { !it.isManuallyEdited }
        val uneditedCount = uneditedSplits.size
        
        val finalSplits = if (uneditedCount > 0) {
            val remaining = totalAmount - editedSum
            val share = if (remaining > 0.0) remaining / uneditedCount else 0.0
            
            val equalShare = Math.round(share * 100.0) / 100.0
            val remainderCents = Math.round((remaining - equalShare * uneditedCount) * 100.0) / 100.0
            
            val newUnedited = uneditedSplits.map { it.copy(amount = "%.2f".format(equalShare)) }.toMutableList()
            
            if (newUnedited.isNotEmpty() && remainderCents != 0.0) {
                val paidByMemberId = _uiState.value.paidByMemberId
                val targetIndex = newUnedited.indexOfFirst { it.member.id == paidByMemberId }.takeIf { it != -1 } ?: 0
                val currentAmount = newUnedited[targetIndex].amount.toDoubleOrNull() ?: 0.0
                newUnedited[targetIndex] = newUnedited[targetIndex].copy(amount = "%.2f".format(currentAmount + remainderCents))
            }
            
            updatedSplits.map { split ->
                if (!split.isManuallyEdited) {
                    newUnedited.first { it.member.id == split.member.id }
                } else {
                    split
                }
            }
        } else {
            updatedSplits
        }
        
        val totalSum = finalSplits.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        val diff = Math.round((totalSum - totalAmount) * 100.0) / 100.0
        val errorMsg = if (diff > 0.0) {
            "Split amounts (${"%.2f".format(totalSum)}) cannot exceed total amount (${"%.2f".format(totalAmount)})"
        } else {
            null
        }
        
        _uiState.update { state ->
            state.copy(
                memberSplits = finalSplits,
                uiError = errorMsg
            )
        }
    }

    fun saveExpense() {
        val state = _uiState.value
        val totalAmount = state.amount.toDoubleOrNull()
        
        if (state.title.isBlank()) {
            _uiState.update { it.copy(uiError = "Title cannot be empty") }
            return
        }
        if (totalAmount == null || totalAmount <= 0) {
            _uiState.update { it.copy(uiError = "Invalid amount") }
            return
        }
        
        val splitSum = state.memberSplits.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        if (Math.abs(splitSum - totalAmount) > 0.01) {
            _uiState.update { it.copy(uiError = "Split amounts ($splitSum) must equal total ($totalAmount)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, uiError = null) }
            try {
                val expense = Expense(
                    id = state.expenseId ?: 0,
                    title = state.title,
                    amount = totalAmount,
                    categoryId = state.categoryId,
                    date = state.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
                    paidByMemberId = state.paidByMemberId,
                    month = state.date.monthValue,
                    year = state.date.year,
                    splitType = state.splitType
                )
                
                val currentExpenseId = if (state.expenseId != null) {
                    repository.updateExpense(expense)
                    val oldSplits = repository.getSplitsByExpense(state.expenseId).first()
                    oldSplits.forEach { repository.deleteSplit(it) }
                    state.expenseId
                } else {
                    repository.insertExpense(expense)
                }
                
                state.memberSplits.forEach { memberSplit ->
                    val isPaidByAll = state.paidByMemberId == null
                    val isPaidByThisMember = memberSplit.member.id == state.paidByMemberId
                    
                    val amountOwed = memberSplit.amount.toDoubleOrNull() ?: 0.0
                    val amountPaid = if (isPaidByAll) amountOwed else if (isPaidByThisMember) totalAmount else 0.0

                    val split = Split(
                        expenseId = currentExpenseId,
                        memberId = memberSplit.member.id,
                        amountOwed = amountOwed,
                        amountPaid = amountPaid,
                        isPaid = isPaidByAll || isPaidByThisMember
                    )
                    repository.insertSplit(split)
                }
                
                val isEdit = state.expenseId != null
                _uiState.update { it.copy(isSaving = false, success = true) }
                SnackbarManager.showMessage(
                    if (isEdit) "Expense \"${state.title}\" updated successfully!"
                    else "Expense \"${state.title}\" added successfully!"
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, uiError = e.message ?: "Failed to save expense") }
            }
        }
    }

    fun loadExpenseForEdit(expenseId: Long) {
        viewModelScope.launch {
            try {
                val expense = repository.getExpenseById(expenseId) ?: return@launch
                val splits = repository.getSplitsByExpense(expenseId).first()
                val household = repository.getAllHouseholds().first().firstOrNull()
                val members = household?.let { repository.getMembersByHousehold(it.id).first() } ?: emptyList()
                
                val memberSplits = members.map { member ->
                    val split = splits.find { it.memberId == member.id }
                    val amountStr = split?.let { "%.2f".format(it.amountOwed) } ?: "0.00"
                    MemberSplitState(
                        member = member,
                        amount = amountStr,
                        isManuallyEdited = expense.splitType == SplitType.CUSTOM
                    )
                }

                val loadedDate = java.time.Instant.ofEpochMilli(expense.date)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()

                _uiState.update { state ->
                    state.copy(
                        expenseId = expense.id,
                        title = expense.title,
                        amount = "%.2f".format(expense.amount),
                        categoryId = expense.categoryId,
                        date = loadedDate,
                        paidByMemberId = expense.paidByMemberId,
                        splitType = expense.splitType,
                        memberSplits = memberSplits,
                        uiError = null,
                        success = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(uiError = "Failed to load expense for edit") }
            }
        }
    }

    fun scanReceipt(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanningReceipt = true, uiError = null) }
            try {
                val result = OcrScanner.scanReceipt(context, uri)
                _uiState.update { state ->
                    var updatedState = state.copy(isScanningReceipt = false)
                    result.amount?.let { amt ->
                        val formatted = "%.2f".format(amt)
                        updatedState = updatedState.copy(amount = formatted)
                    }
                    result.title?.let { title ->
                        updatedState = updatedState.copy(title = title)
                    }
                    updatedState
                }
                
                // Recalculate splits since amount changed
                if (_uiState.value.splitType == SplitType.EQUAL) {
                    updateEqualSplits()
                } else if (_uiState.value.splitType == SplitType.CUSTOM) {
                    updateCustomSplits(_uiState.value.memberSplits)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isScanningReceipt = false,
                        uiError = "Failed to scan receipt: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(uiError = null) }
    }

    fun resetState() {
        _uiState.value = AddExpenseUiState()
        loadInitialData()
    }
}
