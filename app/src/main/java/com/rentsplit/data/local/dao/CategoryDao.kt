package com.rentsplit.data.local.dao

import androidx.room.*
import com.rentsplit.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE householdId = :householdId ORDER BY sortOrder ASC, name ASC")
    fun getCategoriesByHousehold(householdId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Query("SELECT * FROM categories WHERE householdId = :householdId AND name = :name LIMIT 1")
    suspend fun getCategoryByName(householdId: Long, name: String): Category?

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesSync(): List<Category>

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}
