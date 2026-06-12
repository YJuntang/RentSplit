package com.rentsplit.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rentsplit.data.local.RentSplitDatabase
import com.rentsplit.data.local.dao.CategoryDao
import com.rentsplit.data.local.dao.ExpenseDao
import com.rentsplit.data.local.dao.HouseholdDao
import com.rentsplit.data.local.dao.MemberDao
import com.rentsplit.data.local.dao.SplitDao
import com.rentsplit.data.local.dao.RentConfigDao
import com.rentsplit.data.repository.RentSplitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Create categories table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `categories` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `householdId` INTEGER NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `iconName` TEXT NOT NULL, 
                    `colorHex` TEXT NOT NULL, 
                    `budgetLimit` REAL, 
                    `sortOrder` INTEGER NOT NULL, 
                    FOREIGN KEY(`householdId`) REFERENCES `households`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
            """.trimIndent())
            
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_householdId` ON `categories` (`householdId`)")
            
            // 2. Query existing households to seed default categories
            val cursor = db.query("SELECT id FROM households")
            val householdIds = mutableListOf<Long>()
            if (cursor.moveToFirst()) {
                do {
                    householdIds.add(cursor.getLong(0))
                } while (cursor.moveToNext())
            }
            cursor.close()
            
            val defaults = listOf(
                Triple("Rent", "Home", "#00D4FF"),
                Triple("Electricity", "ElectricBolt", "#FBBF24"),
                Triple("Water", "WaterDrop", "#60A5FA"),
                Triple("Internet", "Wifi", "#8B5CF6"),
                Triple("Groceries", "ShoppingCart", "#22C55E"),
                Triple("Gas", "LocalGasStation", "#F97316"),
                Triple("Maintenance", "Build", "#94A3B8"),
                Triple("Other", "Category", "#64748B")
            )
            
            val householdCategoryMap = mutableMapOf<Long, MutableMap<String, Long>>()
            
            for (hId in householdIds) {
                householdCategoryMap[hId] = mutableMapOf()
                for (i in defaults.indices) {
                    val (name, icon, color) = defaults[i]
                    db.execSQL("""
                        INSERT INTO `categories` (`householdId`, `name`, `iconName`, `colorHex`, `budgetLimit`, `sortOrder`) 
                        VALUES ($hId, '$name', '$icon', '$color', NULL, $i)
                    """.trimIndent())
                    
                    val idCursor = db.query("SELECT last_insert_rowid()")
                    if (idCursor.moveToFirst()) {
                        val catId = idCursor.getLong(0)
                        householdCategoryMap[hId]!![name.lowercase()] = catId
                    }
                    idCursor.close()
                }
            }
            
            // 3. Create the new expenses table with categoryId and foreign key constraints
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `expenses_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `title` TEXT NOT NULL, 
                    `amount` REAL NOT NULL, 
                    `categoryId` INTEGER, 
                    `date` INTEGER NOT NULL, 
                    `paidByMemberId` INTEGER, 
                    `month` INTEGER NOT NULL, 
                    `year` INTEGER NOT NULL, 
                    `splitType` TEXT NOT NULL, 
                    FOREIGN KEY(`paidByMemberId`) REFERENCES `members`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                    FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
            """.trimIndent())
            
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_paidByMemberId` ON `expenses_new` (`paidByMemberId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses_new` (`categoryId`)")
            
            // 4. Copy data from expenses to expenses_new, mapping category string to categoryId
            val expCursor = db.query("SELECT id, title, amount, category, date, paidByMemberId, month, year, splitType FROM expenses")
            if (expCursor.moveToFirst()) {
                do {
                    val id = expCursor.getLong(0)
                    val title = expCursor.getString(1).replace("'", "''")
                    val amount = expCursor.getDouble(2)
                    val catStr = expCursor.getString(3).lowercase().trim()
                    val date = expCursor.getLong(4)
                    val paidByMemberId = if (expCursor.isNull(5)) "NULL" else expCursor.getLong(5).toString()
                    val month = expCursor.getInt(6)
                    val year = expCursor.getInt(7)
                    val splitType = expCursor.getString(8)
                    
                    var hId: Long? = null
                    if (paidByMemberId != "NULL") {
                        val mCursor = db.query("SELECT householdId FROM members WHERE id = $paidByMemberId")
                        if (mCursor.moveToFirst()) {
                            hId = mCursor.getLong(0)
                        }
                        mCursor.close()
                    }
                    
                    if (hId == null && householdIds.isNotEmpty()) {
                        hId = householdIds.first()
                    }
                    
                    var catIdStr = "NULL"
                    if (hId != null) {
                        val catMap = householdCategoryMap[hId]
                        if (catMap != null) {
                            val mappedName = when (catStr) {
                                "rent" -> "rent"
                                "electric", "electricity" -> "electricity"
                                "water" -> "water"
                                "internet" -> "internet"
                                "groceries" -> "groceries"
                                "gas" -> "gas"
                                "maintenance" -> "maintenance"
                                else -> "other"
                            }
                            val cId = catMap[mappedName] ?: catMap["other"]
                            if (cId != null) {
                                catIdStr = cId.toString()
                            }
                        }
                    }
                    
                    db.execSQL("""
                        INSERT INTO `expenses_new` (`id`, `title`, `amount`, `categoryId`, `date`, `paidByMemberId`, `month`, `year`, `splitType`) 
                        VALUES ($id, '$title', $amount, $catIdStr, $date, $paidByMemberId, $month, $year, '$splitType')
                    """.trimIndent())
                } while (expCursor.moveToNext())
            }
            expCursor.close()
            
            // 5. Drop old table and rename new table
            db.execSQL("DROP TABLE `expenses`")
            db.execSQL("ALTER TABLE `expenses_new` RENAME TO `expenses`")
            
            // Recreate indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_paidByMemberId` ON `expenses` (`paidByMemberId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `members` ADD COLUMN `isHouseLeader` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `rent_config` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `householdId` INTEGER NOT NULL, 
                    `amount` REAL NOT NULL, 
                    `dueDayOfMonth` INTEGER NOT NULL, 
                    `lastGeneratedMonth` INTEGER NOT NULL, 
                    `lastGeneratedYear` INTEGER NOT NULL, 
                    FOREIGN KEY(`householdId`) REFERENCES `households`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_rent_config_householdId` ON `rent_config` (`householdId`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RentSplitDatabase {
        return Room.databaseBuilder(
            context,
            RentSplitDatabase::class.java,
            "rentsplit_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
    }

    @Provides
    fun provideHouseholdDao(database: RentSplitDatabase): HouseholdDao {
        return database.householdDao()
    }

    @Provides
    fun provideMemberDao(database: RentSplitDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    fun provideCategoryDao(database: RentSplitDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideExpenseDao(database: RentSplitDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideSplitDao(database: RentSplitDatabase): SplitDao {
        return database.splitDao()
    }

    @Provides
    fun provideRentConfigDao(database: RentSplitDatabase): RentConfigDao {
        return database.rentConfigDao()
    }

    @Provides
    @Singleton
    fun provideRepository(
        database: RentSplitDatabase,
        householdDao: HouseholdDao,
        memberDao: MemberDao,
        categoryDao: CategoryDao,
        expenseDao: ExpenseDao,
        splitDao: SplitDao,
        rentConfigDao: RentConfigDao
    ): RentSplitRepository {
        return RentSplitRepository(database, householdDao, memberDao, categoryDao, expenseDao, splitDao, rentConfigDao)
    }
}
