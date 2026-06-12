package com.rentsplit.ui.home

import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Member
import java.time.YearMonth
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MemberSummary(
    val member: Member,
    val totalPaid: Double,
    val totalOwed: Double,
    val balance: Double // positive means they are owed, negative means they owe
)

data class CategoryBudgetSummary(
    val category: Category,
    val totalSpent: Double
)


data class HomeUiState(
    val selectedDate: YearMonth = YearMonth.now(),
    val totalExpenses: Double = 0.0,
    val memberStats: ImmutableList<MemberSummary> = persistentListOf(),
    val spendingHistory: ImmutableList<Pair<String, Double>> = persistentListOf(),
    val categoryBudgets: ImmutableList<CategoryBudgetSummary> = persistentListOf(),
    val isLoading: Boolean = false,
    val uiError: String? = null,
    val currentHouseholdId: Long? = null
)
