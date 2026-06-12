package com.rentsplit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.rentsplit.data.local.dao.CategoryDao
import com.rentsplit.data.local.dao.ExpenseDao
import com.rentsplit.data.local.dao.HouseholdDao
import com.rentsplit.data.local.dao.MemberDao
import com.rentsplit.data.local.dao.SplitDao
import com.rentsplit.data.local.dao.RentConfigDao
import com.rentsplit.data.model.*

class Converters {
    @TypeConverter
    fun fromSplitType(value: SplitType): String {
        return value.name
    }

    @TypeConverter
    fun toSplitType(value: String): SplitType {
        return SplitType.valueOf(value)
    }
}

@Database(
    entities = [Household::class, Member::class, Category::class, Expense::class, Split::class, RentConfig::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RentSplitDatabase : RoomDatabase() {
    abstract fun householdDao(): HouseholdDao
    abstract fun memberDao(): MemberDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun splitDao(): SplitDao
    abstract fun rentConfigDao(): RentConfigDao
}
