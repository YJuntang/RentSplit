package com.rentsplit.ui.home

import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.SplitType
import com.rentsplit.data.preferences.UserPreferences
import com.rentsplit.data.preferences.UserPreferencesRepository
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RentSplitRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: AddExpenseViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        preferencesRepository = mockk()
        
        val prefs = UserPreferences(rentDueDay = 1, notificationsEnabled = false, appTheme = "SYSTEM", defaultSplitType = "EQUAL", hasCompletedOnboarding = true)
        coEvery { preferencesRepository.userPreferencesFlow } returns flowOf(prefs)
        
        val householdId = 1L
        val household = Household(id = householdId, name = "Test House")
        val member1 = Member(id = 1L, householdId = householdId, name = "Alice", colorHex = "#000000")
        val member2 = Member(id = 2L, householdId = householdId, name = "Bob", colorHex = "#000000")
        val category = Category(id = 1L, householdId = householdId, name = "Groceries", iconName = "cart", colorHex = "#000000")
        
        coEvery { repository.getAllHouseholds() } returns flowOf(listOf(household))
        coEvery { repository.getMembersByHousehold(householdId) } returns flowOf(listOf(member1, member2))
        coEvery { repository.getCategoriesByHousehold(householdId) } returns flowOf(listOf(category))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test negative amount input`() = runTest {
        viewModel = AddExpenseViewModel(repository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTitleChange("Rent")
        viewModel.onAmountChange("-500.0")
        viewModel.saveExpense()
        
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull("Should have an error for invalid amount", state.uiError)
        assertEquals("Invalid amount", state.uiError)
    }

    @Test
    fun `test custom split with negative values`() = runTest {
        viewModel = AddExpenseViewModel(repository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTitleChange("Groceries")
        viewModel.onAmountChange("100.0")
        viewModel.onSplitTypeChange(SplitType.CUSTOM)
        
        // Alice pays -20, Bob should pay 120
        viewModel.onMemberSplitAmountChange(1L, "-20.0")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        val bobSplit = state.memberSplits.find { it.member.id == 2L }
        
        // Total = 100. Alice = -20. Remaining = 120. Bob = 120.
        assertEquals("120.00", bobSplit?.amount)
        
        // However, we should verify if saveExpense handles it correctly, or if we should add validation
        // In the current logic, negative splits are technically calculated, but might not be desirable.
        // For the sake of this test, we verify the calculation handles it.
    }

    @Test
    fun `test empty title validation`() = runTest {
        viewModel = AddExpenseViewModel(repository, preferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTitleChange("")
        viewModel.onAmountChange("100.0")
        viewModel.saveExpense()
        
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.uiError)
        assertEquals("Title cannot be empty", state.uiError)
    }
}
