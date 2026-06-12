package com.rentsplit.data.local.dao

import androidx.room.*
import com.rentsplit.data.model.RentConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface RentConfigDao {
    @Query("SELECT * FROM rent_config WHERE householdId = :householdId LIMIT 1")
    fun getRentConfig(householdId: Long): Flow<RentConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rentConfig: RentConfig)
}
