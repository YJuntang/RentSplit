package com.rentsplit.data.repository

import com.rentsplit.data.local.dao.CategoryDao
import com.rentsplit.data.local.dao.ExpenseDao
import com.rentsplit.data.local.dao.HouseholdDao
import com.rentsplit.data.local.dao.MemberDao
import com.rentsplit.data.local.dao.SplitDao
import com.rentsplit.data.local.dao.RentConfigDao
import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Expense
import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.Split
import com.rentsplit.data.model.RentConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

import androidx.room.withTransaction
import com.rentsplit.data.local.RentSplitDatabase

@Singleton
class RentSplitRepository @Inject constructor(
    private val database: RentSplitDatabase,
    private val householdDao: HouseholdDao,
    private val memberDao: MemberDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val splitDao: SplitDao,
    private val rentConfigDao: RentConfigDao
) {
    // Household operations
    fun getAllHouseholds(): Flow<List<Household>> = householdDao.getAllHouseholds()
    suspend fun insertHousehold(household: Household) = householdDao.insert(household)
    suspend fun updateHousehold(household: Household) = householdDao.update(household)
    suspend fun deleteHousehold(household: Household) = householdDao.delete(household)

    // Member operations
    fun getMembersByHousehold(householdId: Long): Flow<List<Member>> = memberDao.getMembersByHousehold(householdId)
    suspend fun insertMember(member: Member) = memberDao.insert(member)
    suspend fun updateMember(member: Member) = memberDao.update(member)
    suspend fun deleteMember(member: Member) = memberDao.delete(member)

    // Category operations
    fun getCategoriesByHousehold(householdId: Long): Flow<List<Category>> = categoryDao.getCategoriesByHousehold(householdId)
    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)
    suspend fun getCategoryByName(householdId: Long, name: String): Category? = categoryDao.getCategoryByName(householdId, name)
    suspend fun insertCategory(category: Category) = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // Expense operations
    fun getExpensesByMonth(month: Int, year: Int): Flow<List<Expense>> = expenseDao.getExpensesByMonth(month, year)
    fun searchExpensesByMonth(month: Int, year: Int, query: String): Flow<List<Expense>> = expenseDao.searchExpensesByMonth(month, year, query)
    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)
    fun getMonthsWithExpenses(): Flow<List<ExpenseDao.MonthYear>> = expenseDao.getMonthsWithExpenses()
    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    // Split operations
    fun getSplitsByExpense(expenseId: Long): Flow<List<Split>> = splitDao.getSplitsByExpense(expenseId)
    fun getSplitsForExpenses(expenseIds: List<Long>): Flow<List<Split>> = splitDao.getSplitsForExpenses(expenseIds)
    fun getSplitsByMember(memberId: Long): Flow<List<Split>> = splitDao.getSplitsByMember(memberId)
    fun getTotalOwedByMember(memberId: Long): Flow<Double?> = splitDao.getTotalOwedByMember(memberId)
    suspend fun insertSplit(split: Split) = splitDao.insert(split)
    suspend fun updateSplit(split: Split) = splitDao.update(split)
    suspend fun deleteSplit(split: Split) = splitDao.delete(split)

    // RentConfig operations
    fun getRentConfig(householdId: Long): Flow<RentConfig?> = rentConfigDao.getRentConfig(householdId)
    suspend fun insertOrUpdateRentConfig(rentConfig: RentConfig) = rentConfigDao.insertOrUpdate(rentConfig)

    // Backup & Restore operations
    suspend fun getAllHouseholdsSync(): List<Household> = householdDao.getAllHouseholdsSync()
    suspend fun getAllMembersSync(): List<Member> = memberDao.getAllMembersSync()
    suspend fun getAllCategoriesSync(): List<Category> = categoryDao.getAllCategoriesSync()
    suspend fun getAllExpensesSync(): List<Expense> = expenseDao.getAllExpensesSync()
    suspend fun getAllSplitsSync(): List<Split> = splitDao.getAllSplitsSync()

    suspend fun restoreFullDatabase(
        households: List<Household>,
        members: List<Member>,
        categories: List<Category>,
        expenses: List<Expense>,
        splits: List<Split>
    ) {
        database.withTransaction {
            // Clear existing data
            splitDao.deleteAllSplits()
            expenseDao.deleteAllExpenses()
            categoryDao.deleteAllCategories()
            memberDao.deleteAllMembers()
            householdDao.deleteAllHouseholds()

            // Insert new data
            households.forEach { householdDao.insert(it) }
            members.forEach { memberDao.insert(it) }
            categories.forEach { categoryDao.insert(it) }
            expenses.forEach { expenseDao.insert(it) }
            splits.forEach { splitDao.insert(it) }
        }
    }
}
