package com.rentsplit.data.local.dao

import androidx.room.*
import com.rentsplit.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE month = :month AND year = :year")
    fun getExpensesByMonth(month: Int, year: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE month = :month AND year = :year AND title LIKE '%' || :query || '%'")
    fun searchExpensesByMonth(month: Int, year: Int, query: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE paidByMemberId = :memberId")
    fun getExpensesByMember(memberId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT DISTINCT month, year FROM expenses ORDER BY year DESC, month DESC")
    fun getMonthsWithExpenses(): Flow<List<MonthYear>>

    data class MonthYear(val month: Int, val year: Int)

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSync(): List<Expense>

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}
