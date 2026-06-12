package com.rentsplit.data.local.dao

import androidx.room.*
import com.rentsplit.data.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: Member): Long

    @Update
    suspend fun update(member: Member)

    @Delete
    suspend fun delete(member: Member)

    @Query("SELECT * FROM members WHERE householdId = :householdId")
    fun getMembersByHousehold(householdId: Long): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Long): Member?

    @Query("SELECT * FROM members")
    suspend fun getAllMembersSync(): List<Member>

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()
}
