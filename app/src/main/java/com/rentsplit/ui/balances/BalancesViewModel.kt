package com.rentsplit.ui.balances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentsplit.data.model.Member
import com.rentsplit.data.repository.RentSplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Data Models ───────────────────────────────────────────────────────────────

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class BalanceEntry(
    val fromMember: Member,   // this member owes
    val toMember: Member,     // this member is owed
    val amount: Double        // positive value = fromMember owes toMember this amount
)

data class BalancesUiState(
    val balances: ImmutableList<BalanceEntry> = persistentListOf(),
    val totalOwed: Double = 0.0,
    val isLoading: Boolean = true,
    val uiError: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class BalancesViewModel @Inject constructor(
    private val repository: RentSplitRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BalancesUiState> = repository.getAllHouseholds()
        .flatMapLatest { households ->
            val household = households.firstOrNull()
                ?: return@flatMapLatest flowOf(BalancesUiState(isLoading = false))

            repository.getMembersByHousehold(household.id).flatMapLatest { members ->
                if (members.size < 2) {
                    return@flatMapLatest flowOf(
                        BalancesUiState(isLoading = false)
                    )
                }

                // Collect all split IDs across all expenses
                val expenseIds = repository.getMonthsWithExpenses()
                    .flatMapLatest { months ->
                        if (months.isEmpty()) return@flatMapLatest flowOf(emptyList())
                        combine(
                            months.map { my -> repository.getExpensesByMonth(my.month, my.year) }
                        ) { expenseLists -> expenseLists.toList().flatten() }
                    }
                    .map { expenses -> expenses.map { it.id } }

                expenseIds.flatMapLatest { ids ->
                    if (ids.isEmpty()) {
                        return@flatMapLatest flowOf(BalancesUiState(isLoading = false))
                    }
                    repository.getSplitsForExpenses(ids).map { splits ->

                        // the payer is owed (amountPaid - amountOwed) distributed among non-payers
                        // We track: memberBalance[id] = total_paid - total_owed (positive = is owed)
                        val memberBalance = mutableMapOf<Long, Double>()
                        members.forEach { memberBalance[it.id] = 0.0 }

                        for (split in splits) {
                            val current = memberBalance[split.memberId] ?: 0.0
                            memberBalance[split.memberId] = current + split.amountPaid - split.amountOwed
                        }

                        // Debt simplification: minimize transactions
                        // Members with positive balance are owed, negative balance owe
                        data class Balance(val memberId: Long, var amount: Double)

                        val creditors = memberBalance.entries
                            .filter { it.value > 0.005 }
                            .map { Balance(it.key, it.value) }
                            .sortedByDescending { it.amount }
                            .toMutableList()
                        val debtors = memberBalance.entries
                            .filter { it.value < -0.005 }
                            .map { Balance(it.key, it.value) }
                            .sortedBy { it.amount }
                            .toMutableList()

                        val entries = mutableListOf<BalanceEntry>()
                        var i = 0; var j = 0
                        while (i < creditors.size && j < debtors.size) {
                            val creditor = creditors[i]
                            val debtor = debtors[j]
                            val amount = minOf(creditor.amount, -debtor.amount)

                            if (amount > 0.005) {
                                val fromMember = members.find { it.id == debtor.memberId }
                                val toMember = members.find { it.id == creditor.memberId }
                                if (fromMember != null && toMember != null) {
                                    entries.add(BalanceEntry(fromMember, toMember, amount))
                                }
                            }

                            creditor.amount -= amount
                            debtor.amount += amount

                            if (creditor.amount < 0.005) i++
                            if (-debtor.amount < 0.005) j++
                        }


                        val totalOwed = entries.sumOf { it.amount }

                        BalancesUiState(
                            balances = entries.toImmutableList(),
                            totalOwed = totalOwed,
                            isLoading = false
                        )
                    }
                }
            }
        }
        .catch { e ->
            emit(BalancesUiState(isLoading = false, uiError = e.message ?: "Failed to load balances"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalancesUiState())

    fun settleUp(entry: BalanceEntry) {
        viewModelScope.launch {
            try {
                val cal = java.util.Calendar.getInstance()
                val month = cal.get(java.util.Calendar.MONTH) + 1
                val year = cal.get(java.util.Calendar.YEAR)

                val expense = com.rentsplit.data.model.Expense(
                    title = "Settlement: ${entry.fromMember.name} to ${entry.toMember.name}",
                    amount = entry.amount,
                    categoryId = null,
                    date = System.currentTimeMillis(),
                    paidByMemberId = entry.fromMember.id,
                    month = month,
                    year = year,
                    splitType = com.rentsplit.data.model.SplitType.SETTLEMENT
                )

                val expenseId = repository.insertExpense(expense)

                val fromSplit = com.rentsplit.data.model.Split(
                    expenseId = expenseId,
                    memberId = entry.fromMember.id,
                    amountOwed = 0.0,
                    amountPaid = entry.amount,
                    isPaid = true
                )

                val toSplit = com.rentsplit.data.model.Split(
                    expenseId = expenseId,
                    memberId = entry.toMember.id,
                    amountOwed = entry.amount,
                    amountPaid = 0.0,
                    isPaid = false
                )

                repository.insertSplit(fromSplit)
                repository.insertSplit(toSplit)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
