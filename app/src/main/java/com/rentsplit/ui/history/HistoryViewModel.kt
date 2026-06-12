package com.rentsplit.ui.history

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Expense
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.Split
import com.rentsplit.data.model.SplitType
import com.rentsplit.data.repository.RentSplitRepository
import com.rentsplit.util.ExportUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

enum class SortOrder {
    DATE_NEWEST, DATE_OLDEST, AMOUNT_HIGHEST, AMOUNT_LOWEST, NAME_A_Z
}

data class MonthlySummary(
    val month: Int,
    val year: Int,
    val monthName: String,
    val totalAmount: Double
)

data class HistoryUiState(
    val monthlySummaries: List<MonthlySummary> = emptyList(),
    val isLoading: Boolean = false
)

data class MonthDetailUiState(
    val month: Int = 0,
    val year: Int = 0,
    val monthName: String = "",
    val expenses: List<ExpenseDetail> = emptyList(),
    val members: List<Member> = emptyList(),
    val isLoading: Boolean = false
)

data class ExpenseDetail(
    val expense: Expense,
    val paidByMember: Member?,
    val splits: List<Split>,
    val category: Category? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: RentSplitRepository
) : ViewModel() {

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError = _uiError.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyUiState: StateFlow<HistoryUiState> = repository.getMonthsWithExpenses()
        .flatMapLatest { months ->
            val flows = months.map { my ->
                repository.getExpensesByMonth(my.month, my.year).map { expenses ->
                    MonthlySummary(
                        month = my.month,
                        year = my.year,
                        monthName = Month.of(my.month).getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        totalAmount = expenses.filter { it.splitType != SplitType.SETTLEMENT }.sumOf { it.amount }
                    )
                }
            }
            if (flows.isEmpty()) flowOf(emptyList())
            else combine(flows) { it.toList() }
        }
        .map { HistoryUiState(monthlySummaries = it, isLoading = false) }
        .catch { e ->
            _uiError.value = "Failed to load history"
            emit(HistoryUiState())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState(isLoading = true))

    private val _monthDetailUiState = MutableStateFlow(MonthDetailUiState())
    val monthDetailUiState: StateFlow<MonthDetailUiState> = _monthDetailUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_NEWEST)
    val sortOrder = _sortOrder.asStateFlow()

    private val _currentMonthYear = MutableStateFlow<Pair<Int, Int>?>(null)

    private var recentlyDeletedExpense: Expense? = null
    private var recentlyDeletedSplits: List<Split> = emptyList()

    init {
        viewModelScope.launch {
            _currentMonthYear.filterNotNull().flatMapLatest { monthYear ->
                val (month, year) = monthYear
                _searchQuery.flatMapLatest { query ->
                    if (query.isBlank()) {
                        repository.getExpensesByMonth(month, year)
                    } else {
                        repository.searchExpensesByMonth(month, year, query)
                    }
                }
            }.combine(_sortOrder) { expenses, sortOrder ->
                Pair(expenses, sortOrder)
            }.collectLatest { (expenses, sortOrder) ->
                try {
                    val household = repository.getAllHouseholds().first().firstOrNull()
                    val members = household?.let { repository.getMembersByHousehold(it.id).first() } ?: emptyList()
                    val categories = household?.let { repository.getCategoriesByHousehold(it.id).first() } ?: emptyList()
                    
                    val details = expenses.map { expense ->
                        val splits = repository.getSplitsByExpense(expense.id).first()
                        val paidBy = members.find { it.id == expense.paidByMemberId }
                        val category = categories.find { it.id == expense.categoryId }
                        ExpenseDetail(expense, paidBy, splits, category)
                    }
                    
                    val sortedDetails = when (sortOrder) {
                        SortOrder.DATE_NEWEST -> details.sortedByDescending { it.expense.date }
                        SortOrder.DATE_OLDEST -> details.sortedBy { it.expense.date }
                        SortOrder.AMOUNT_HIGHEST -> details.sortedByDescending { it.expense.amount }
                        SortOrder.AMOUNT_LOWEST -> details.sortedBy { it.expense.amount }
                        SortOrder.NAME_A_Z -> details.sortedBy { it.expense.title.lowercase() }
                    }
                    
                    _monthDetailUiState.update { it.copy(expenses = sortedDetails, members = members, isLoading = false) }
                } catch (e: Exception) {
                    _uiError.value = "Failed to update details list"
                    _monthDetailUiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun loadMonthDetails(month: Int, year: Int) {
        _monthDetailUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                _monthDetailUiState.update { 
                    it.copy(
                        month = month, 
                        year = year, 
                        monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
                    ) 
                }
                
                val members = repository.getAllHouseholds().first().firstOrNull()?.let {
                    repository.getMembersByHousehold(it.id).first()
                } ?: emptyList()
                
                _monthDetailUiState.update { it.copy(members = members) }
                
                _currentMonthYear.value = Pair(month, year)
            } catch (e: Exception) {
                _uiError.value = "Failed to load month details"
                _monthDetailUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteExpense(expenseDetail: ExpenseDetail) {
        viewModelScope.launch {
            try {
                recentlyDeletedExpense = expenseDetail.expense
                recentlyDeletedSplits = expenseDetail.splits
                repository.deleteExpense(expenseDetail.expense)
            } catch (e: Exception) {
                _uiError.value = "Failed to delete expense"
            }
        }
    }

    fun undoDelete() {
        val expense = recentlyDeletedExpense ?: return
        val splits = recentlyDeletedSplits
        viewModelScope.launch {
            try {
                repository.insertExpense(expense)
                for (split in splits) {
                    repository.insertSplit(split)
                }
                recentlyDeletedExpense = null
                recentlyDeletedSplits = emptyList()
            } catch (e: Exception) {
                _uiError.value = "Failed to restore expense"
            }
        }
    }

    fun exportMonthToCsv(context: Context) {
        val state = _monthDetailUiState.value
        viewModelScope.launch {
            try {
                val csvContent = ExportUtils.generateCsv(state.expenses, state.members)
                val fileName = "RentSplit_${state.monthName}_${state.year}.csv"
                val file = File(context.externalCacheDir, fileName)
                file.writeText(csvContent)
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Expenses for ${state.monthName} ${state.year}")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(intent, "Export Expenses"))
            } catch (e: Exception) {
                _uiError.value = "Failed to export CSV"
            }
        }
    }

    fun clearError() {
        _uiError.value = null
    }
}
