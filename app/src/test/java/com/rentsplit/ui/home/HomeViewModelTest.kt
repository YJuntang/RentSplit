package com.rentsplit.ui.home

import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Expense
import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.Split
import com.rentsplit.data.model.SplitType
import com.rentsplit.data.repository.RentSplitRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RentSplitRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when data is loaded successfully, state is updated with expenses and splits`() = runTest {
        // Arrange
        val householdId = 1L
        val household = Household(id = householdId, name = "Test House")
        val member1 = Member(id = 1L, householdId = householdId, name = "Alice", colorHex = "#000000")
        val member2 = Member(id = 2L, householdId = householdId, name = "Bob", colorHex = "#000000")
        val category = Category(id = 1L, householdId = householdId, name = "Groceries", iconName = "cart", colorHex = "#000000")
        
        val expense = Expense(
            id = 100L,
            title = "Apples",
            amount = 50.0,
            categoryId = category.id,
            date = 0L,
            paidByMemberId = member1.id,
            month = YearMonth.now().monthValue,
            year = YearMonth.now().year,
            splitType = SplitType.EQUAL
        )

        val split1 = Split(expenseId = 100L, memberId = 1L, amountOwed = 25.0, amountPaid = 50.0, isPaid = true)
        val split2 = Split(expenseId = 100L, memberId = 2L, amountOwed = 25.0, amountPaid = 0.0, isPaid = true)

        coEvery { repository.getAllHouseholds() } returns flowOf(listOf(household))
        coEvery { repository.getExpensesByMonth(any(), any()) } returns flowOf(listOf(expense))
        coEvery { repository.getMembersByHousehold(householdId) } returns flowOf(listOf(member1, member2))
        coEvery { repository.getCategoriesByHousehold(householdId) } returns flowOf(listOf(category))
        coEvery { repository.getSplitsForExpenses(listOf(100L)) } returns flowOf(listOf(split1, split2))

        // Act
        viewModel = HomeViewModel(repository)
        
        val collectJob = backgroundScope.launch {
            viewModel.uiState.collect()
        }
        
        // Let coroutines run
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(householdId, state.currentHouseholdId)
        assertEquals(50.0, state.totalExpenses, 0.0)
        
        // Alice paid 50, owed 25. Balance = +25
        val aliceStats = state.memberStats.find { it.member.id == 1L }
        assertEquals(25.0, aliceStats?.balance ?: 0.0, 0.0)

        // Bob paid 0, owed 25. Balance = -25
        val bobStats = state.memberStats.find { it.member.id == 2L }
        assertEquals(-25.0, bobStats?.balance ?: 0.0, 0.0)
        
        collectJob.cancel()
    }

    @Test
    fun `test extreme split handling`() = runTest {
        // Arrange
        val householdId = 1L
        val household = Household(id = householdId, name = "Rich House")
        val member = Member(id = 1L, householdId = householdId, name = "Elon", colorHex = "#000000")
        val category = Category(id = 1L, householdId = householdId, name = "Rockets", iconName = "rocket", colorHex = "#000000")
        
        val extremeAmount = 1_000_000_000.0 // 1 Billion

        val expense = Expense(
            id = 101L,
            title = "SpaceX",
            amount = extremeAmount,
            categoryId = category.id,
            date = 0L,
            paidByMemberId = member.id,
            month = YearMonth.now().monthValue,
            year = YearMonth.now().year,
            splitType = SplitType.EQUAL
        )

        val split = Split(expenseId = 101L, memberId = 1L, amountOwed = extremeAmount, amountPaid = extremeAmount, isPaid = true)

        coEvery { repository.getAllHouseholds() } returns flowOf(listOf(household))
        coEvery { repository.getExpensesByMonth(any(), any()) } returns flowOf(listOf(expense))
        coEvery { repository.getMembersByHousehold(householdId) } returns flowOf(listOf(member))
        coEvery { repository.getCategoriesByHousehold(householdId) } returns flowOf(listOf(category))
        coEvery { repository.getSplitsForExpenses(listOf(101L)) } returns flowOf(listOf(split))

        // Act
        viewModel = HomeViewModel(repository)
        
        val collectJob = backgroundScope.launch {
            viewModel.uiState.collect()
        }
        
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(extremeAmount, state.totalExpenses, 0.0)
        
        collectJob.cancel()
    }
}
