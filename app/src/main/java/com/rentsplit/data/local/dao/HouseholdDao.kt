package com.rentsplit.data.local.dao

import androidx.room.*
import com.rentsplit.data.model.Household
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(household: Household): Long

    @Update
    suspend fun update(household: Household)

    @Delete
    suspend fun delete(household: Household)

    @Query("SELECT * FROM households")
    fun getAllHouseholds(): Flow<List<Household>>

    @Query("SELECT * FROM households WHERE id = :id")
    suspend fun getHouseholdById(id: Long): Household?

    @Query("SELECT * FROM households")
    suspend fun getAllHouseholdsSync(): List<Household>

    @Query("DELETE FROM households")
    suspend fun deleteAllHouseholds()
}
