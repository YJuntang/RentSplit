package com.rentsplit.data.local.dao

import androidx.room.*
import com.rentsplit.data.model.Split
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(split: Split): Long

    @Update
    suspend fun update(split: Split)

    @Delete
    suspend fun delete(split: Split)

    @Query("SELECT * FROM splits WHERE expenseId = :expenseId")
    fun getSplitsByExpense(expenseId: Long): Flow<List<Split>>

    @Query("SELECT * FROM splits WHERE memberId = :memberId")
    fun getSplitsByMember(memberId: Long): Flow<List<Split>>

    @Query("SELECT * FROM splits WHERE expenseId IN (:expenseIds)")
    fun getSplitsForExpenses(expenseIds: List<Long>): Flow<List<Split>>

    @Query("SELECT SUM(amountOwed - amountPaid) FROM splits WHERE memberId = :memberId AND isPaid = 0")
    fun getTotalOwedByMember(memberId: Long): Flow<Double?>

    @Query("SELECT * FROM splits")
    suspend fun getAllSplitsSync(): List<Split>

    @Query("DELETE FROM splits")
    suspend fun deleteAllSplits()
}
