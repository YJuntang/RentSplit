package com.rentsplit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentsplit.data.model.Expense
import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.Split
import com.rentsplit.data.model.SplitType
import com.rentsplit.data.repository.RentSplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RentSplitRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(YearMonth.now())
    private val _isLoading = MutableStateFlow(false)
    private val _uiError = MutableStateFlow<String?>(null)

    private val _currentHouseholdId = repository.getAllHouseholds()
        .map { households -> households.firstOrNull()?.id }
        .distinctUntilChanged()

    init {
        checkAndGenerateRent()
    }

    private fun checkAndGenerateRent() {
        viewModelScope.launch {
            val households = repository.getAllHouseholdsSync()
            val household = households.firstOrNull() ?: return@launch
            
            val config = repository.getRentConfig(household.id).firstOrNull() ?: return@launch
            
            val calendar = java.util.Calendar.getInstance()
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1 // 1-12
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            
            if (currentDay >= config.dueDayOfMonth && 
                (config.lastGeneratedMonth != currentMonth || config.lastGeneratedYear != currentYear)) {
                
                val members = repository.getAllMembersSync()
                val houseLeader = members.find { it.isHouseLeader } ?: return@launch
                
                val categories = repository.getAllCategoriesSync()
                val rentCategory = categories.find { it.name.lowercase() == "rent" }
                
                val expense = Expense(
                    title = "Rent",
                    amount = config.amount,
                    categoryId = rentCategory?.id,
                    date = calendar.timeInMillis,
                    paidByMemberId = houseLeader.id,
                    month = currentMonth,
                    year = currentYear,
                    splitType = SplitType.EQUAL
                )
                
                val expenseId = repository.insertExpense(expense)
                
                val splitAmount = Math.round((config.amount / members.size) * 100.0) / 100.0
                val remainder = Math.round((config.amount - (splitAmount * members.size)) * 100.0) / 100.0
                
                members.forEach { member ->
                    val owed = if (member.id == houseLeader.id) {
                        splitAmount + remainder
                    } else {
                        splitAmount
                    }
                    val paid = if (member.id == houseLeader.id) config.amount else 0.0
                    
                    val split = Split(
                        expenseId = expenseId,
                        memberId = member.id,
                        amountOwed = owed,
                        amountPaid = paid,
                        isPaid = paid >= owed
                    )
                    repository.insertSplit(split)
                }
                
                repository.insertOrUpdateRentConfig(
                    config.copy(lastGeneratedMonth = currentMonth, lastGeneratedYear = currentYear)
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        _selectedDate,
        _currentHouseholdId,
        _isLoading,
        _uiError
    ) { date, householdId, isLoading, uiError ->
        HomeUiState(
            selectedDate = date,
            currentHouseholdId = householdId,
            isLoading = isLoading,
            uiError = uiError
        )
    }.flatMapLatest { state ->
        if (state.currentHouseholdId == null) {
            flowOf(state)
        } else {
            val date = state.selectedDate
            val expensesFlow = repository.getExpensesByMonth(date.monthValue, date.year)
            
            // Fetch last 6 months spending history
            val historyFlow = (0..5).reversed().map { offset ->
                val historyDate = YearMonth.now().minusMonths(offset.toLong())
                repository.getExpensesByMonth(historyDate.monthValue, historyDate.year).map { expenses ->
                    val monthName = historyDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    monthName to expenses.filter { it.splitType != SplitType.SETTLEMENT }.sumOf { it.amount }
                }
            }.let { combine(it) { it.toList() } }

            combine(
                expensesFlow,
                repository.getMembersByHousehold(state.currentHouseholdId),
                historyFlow,
                repository.getCategoriesByHousehold(state.currentHouseholdId)
            ) { expenses, members, history, categories ->
                val totalExpenses = expenses.filter { it.splitType != SplitType.SETTLEMENT }.sumOf { it.amount }
                val budgetSummaries = categories.map { category ->
                    val spent = expenses.filter { it.categoryId == category.id && it.splitType != SplitType.SETTLEMENT }.sumOf { it.amount }
                    CategoryBudgetSummary(category, spent)
                }
                state.copy(
                    totalExpenses = totalExpenses,
                    spendingHistory = history.toImmutableList(),
                    categoryBudgets = budgetSummaries.toImmutableList()
                )
            }.flatMapLatest { updatedState ->
                val expenseIds = repository.getExpensesByMonth(updatedState.selectedDate.monthValue, updatedState.selectedDate.year)
                    .map { expenses -> expenses.map { it.id } }
                
                combine(
                    repository.getMembersByHousehold(updatedState.currentHouseholdId!!),
                    expenseIds.flatMapLatest { ids -> 
                        if (ids.isEmpty()) flowOf(emptyList<Split>())
                        else repository.getSplitsForExpenses(ids)
                    }
                ) { members, splits ->
                    val summaries = members.map { member ->
                        val memberSplits = splits.filter { it.memberId == member.id }
                        val paid = memberSplits.sumOf { it.amountPaid }
                        val owed = memberSplits.sumOf { it.amountOwed }
                        MemberSummary(
                            member = member,
                            totalPaid = paid,
                            totalOwed = owed,
                            balance = paid - owed
                        )
                    }
                    updatedState.copy(memberStats = summaries.toImmutableList())
                }
            }
        }
    }.catch { e ->
        _uiError.value = e.message ?: "An unexpected error occurred"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun clearError() {
        _uiError.value = null
    }

    fun nextMonth() {
        _selectedDate.value = _selectedDate.value.plusMonths(1)
    }

    fun previousMonth() {
        _selectedDate.value = _selectedDate.value.minusMonths(1)
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(400)
            _isLoading.value = false
        }
    }
}
