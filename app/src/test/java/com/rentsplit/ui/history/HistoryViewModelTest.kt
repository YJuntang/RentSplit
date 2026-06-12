package com.rentsplit.ui.history

import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Expense
import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.Split
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RentSplitRepository
    private lateinit var viewModel: HistoryViewModel

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
    fun `test loading monthly summaries aggregates amounts correctly`() = runTest {
        // Arrange
        val monthYear = mockk<ExpenseDao.MonthYear>()
        every { monthYear.month } returns 5
        every { monthYear.year } returns 2026
        coEvery { repository.getMonthsWithExpenses() } returns flowOf(listOf(monthYear))
        
        val expense = Expense(
            id = 100L, title = "Rent", amount = 120.0, categoryId = null,
            date = 0L, paidByMemberId = 1L, month = 5, year = 2026, splitType = com.rentsplit.data.model.SplitType.EQUAL
        )
        coEvery { repository.getExpensesByMonth(5, 2026) } returns flowOf(listOf(expense))

        // Act
        viewModel = HistoryViewModel(repository)

        val collectJob = backgroundScope.launch {
            viewModel.historyUiState.collect()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.historyUiState.value
        assertEquals(1, state.monthlySummaries.size)
        assertEquals("May", state.monthlySummaries[0].monthName)
        assertEquals(120.0, state.monthlySummaries[0].totalAmount, 0.0)

        collectJob.cancel()
    }

    @Test
    fun `test month details queries and returns sorted items`() = runTest {
        // Arrange
        val household = Household(id = 1L, name = "House")
        val member = Member(id = 1L, householdId = 1L, name = "Alice", colorHex = "#000000")
        val category = Category(id = 1L, householdId = 1L, name = "Food", iconName = "cart", colorHex = "#000000")
        
        val expense1 = Expense(id = 1L, title = "Apples", amount = 10.0, categoryId = 1L, date = 1000L, paidByMemberId = 1L, month = 5, year = 2026, splitType = com.rentsplit.data.model.SplitType.EQUAL)
        val expense2 = Expense(id = 2L, title = "Bananas", amount = 20.0, categoryId = 1L, date = 2000L, paidByMemberId = 1L, month = 5, year = 2026, splitType = com.rentsplit.data.model.SplitType.EQUAL)

        coEvery { repository.getAllHouseholds() } returns flowOf(listOf(household))
        coEvery { repository.getMembersByHousehold(1L) } returns flowOf(listOf(member))
        coEvery { repository.getCategoriesByHousehold(1L) } returns flowOf(listOf(category))
        coEvery { repository.getExpensesByMonth(5, 2026) } returns flowOf(listOf(expense1, expense2))
        coEvery { repository.getSplitsByExpense(any()) } returns flowOf(emptyList())

        // Act
        viewModel = HistoryViewModel(repository)
        val collectJob = backgroundScope.launch {
            viewModel.monthDetailUiState.collect()
        }
        
        viewModel.loadMonthDetails(5, 2026)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Default sorting is DATE_NEWEST
        val state = viewModel.monthDetailUiState.value
        assertEquals(2, state.expenses.size)
        assertEquals(2L, state.expenses[0].expense.id) // Bananas (date 2000L) is first
        assertEquals(1L, state.expenses[1].expense.id) // Apples (date 1000L) is second

        // Sort by AMOUNT_LOWEST
        viewModel.updateSortOrder(SortOrder.AMOUNT_LOWEST)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateSorted = viewModel.monthDetailUiState.value
        assertEquals(1L, stateSorted.expenses[0].expense.id) // Apples ($10) is first
        assertEquals(2L, stateSorted.expenses[1].expense.id) // Bananas ($20) is second

        collectJob.cancel()
    }

    @Test
    fun `test delete and undo restore cached expense and splits`() = runTest {
        // Arrange
        val expense = Expense(id = 1L, title = "Apples", amount = 10.0, categoryId = null, date = 0L, paidByMemberId = 1L, month = 5, year = 2026, splitType = com.rentsplit.data.model.SplitType.EQUAL)
        val splits = listOf(Split(expenseId = 1L, memberId = 1L, amountOwed = 10.0, amountPaid = 10.0, isPaid = true))
        val detail = ExpenseDetail(expense = expense, paidByMember = null, splits = splits, category = null)

        viewModel = HistoryViewModel(repository)

        // Act - Delete
        viewModel.deleteExpense(detail)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repository.deleteExpense(expense) }

        // Act - Undo
        viewModel.undoDelete()
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repository.insertExpense(expense) }
        coVerify(exactly = 1) { repository.insertSplit(any()) }
    }
}
