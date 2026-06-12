package com.rentsplit.ui.balances

import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.Split
import com.rentsplit.data.model.Expense
import com.rentsplit.data.model.SplitType
import com.rentsplit.data.local.dao.ExpenseDao
import com.rentsplit.data.repository.RentSplitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BalancesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RentSplitRepository
    private lateinit var viewModel: BalancesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test empty and single member households return empty balances`() = runTest {
        val householdId = 1L
        val household = Household(id = householdId, name = "Empty House")
        val member = Member(id = 1L, householdId = householdId, name = "Alone", colorHex = "#000000")

        coEvery { repository.getAllHouseholds() } returns flowOf(listOf(household))
        coEvery { repository.getMembersByHousehold(householdId) } returns flowOf(listOf(member))

        viewModel = BalancesViewModel(repository)

        val collectJob = backgroundScope.launch {
            viewModel.uiState.collect()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.balances.isEmpty())
        assertEquals(0.0, state.totalOwed, 0.0)
        collectJob.cancel()
    }

    @Test
    fun `test debt simplification algorithm minimises transactions`() = runTest {
        // Arrange
        val householdId = 1L
        val household = Household(id = householdId, name = "Simplified House")
        val memberA = Member(id = 1L, householdId = householdId, name = "Alice", colorHex = "#000000")
        val memberB = Member(id = 2L, householdId = householdId, name = "Bob", colorHex = "#000000")
        val memberC = Member(id = 3L, householdId = householdId, name = "Charlie", colorHex = "#000000")

        // Alice paid 90, Bob paid 0, Charlie paid 0. Each owes 30.
        // Balance sheet: Alice: +60, Bob: -30, Charlie: -30.
        // This should simplify to Bob owes Alice 30, Charlie owes Alice 30.
        coEvery { repository.getAllHouseholds() } returns flowOf(listOf(household))
        coEvery { repository.getMembersByHousehold(householdId) } returns flowOf(listOf(memberA, memberB, memberC))
        
        val monthYear = mockk<ExpenseDao.MonthYear>()
        every { monthYear.month } returns 5
        every { monthYear.year } returns 2026
        coEvery { repository.getMonthsWithExpenses() } returns flowOf(listOf(monthYear))
        
        val expense = Expense(
            id = 100L, title = "Rent", amount = 90.0, categoryId = null,
            date = 0L, paidByMemberId = 1L, month = 5, year = 2026, splitType = SplitType.EQUAL
        )
        coEvery { repository.getExpensesByMonth(5, 2026) } returns flowOf(listOf(expense))
        
        val splitA = Split(expenseId = 100L, memberId = 1L, amountOwed = 30.0, amountPaid = 90.0, isPaid = true)
        val splitB = Split(expenseId = 100L, memberId = 2L, amountOwed = 30.0, amountPaid = 0.0, isPaid = false)
        val splitC = Split(expenseId = 100L, memberId = 3L, amountOwed = 30.0, amountPaid = 0.0, isPaid = false)
        coEvery { repository.getSplitsForExpenses(listOf(100L)) } returns flowOf(listOf(splitA, splitB, splitC))

        // Act
        viewModel = BalancesViewModel(repository)

        val collectJob = backgroundScope.launch {
            viewModel.uiState.collect()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(60.0, state.totalOwed, 0.0)
        assertEquals(2, state.balances.size)
        
        val entry1 = state.balances.find { it.fromMember.id == 2L }
        assertEquals(1L, entry1?.toMember?.id)
        assertEquals(30.0, entry1?.amount ?: 0.0, 0.0)

        val entry2 = state.balances.find { it.fromMember.id == 3L }
        assertEquals(1L, entry2?.toMember?.id)
        assertEquals(30.0, entry2?.amount ?: 0.0, 0.0)

        collectJob.cancel()
    }

    @Test
    fun `test settleUp constructs and inserts custom splits`() = runTest {
        // Arrange
        val memberA = Member(id = 1L, householdId = 1L, name = "Alice", colorHex = "#000000")
        val memberB = Member(id = 2L, householdId = 1L, name = "Bob", colorHex = "#000000")
        val entry = BalanceEntry(fromMember = memberB, toMember = memberA, amount = 30.0)

        coEvery { repository.insertExpense(any()) } returns 200L

        // Act
        viewModel = BalancesViewModel(repository)
        viewModel.settleUp(entry)

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.insertExpense(any()) }
        coVerify(exactly = 2) { repository.insertSplit(any()) }
    }
}
