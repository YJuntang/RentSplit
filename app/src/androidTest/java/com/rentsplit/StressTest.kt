package com.rentsplit

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rentsplit.data.local.RentSplitDatabase
import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Expense
import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.SplitType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class StressTest {

    private lateinit var db: RentSplitDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            context, RentSplitDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndLoad10000Expenses() = runBlocking {
        val householdDao = db.householdDao()
        val memberDao = db.memberDao()
        val categoryDao = db.categoryDao()
        val expenseDao = db.expenseDao()

        val householdId = householdDao.insert(Household(name = "Stress House"))
        val memberId = memberDao.insert(Member(householdId = householdId, name = "Stressor", colorHex = "#000"))
        val categoryId = categoryDao.insert(Category(householdId = householdId, name = "Test", iconName = "test", colorHex = "#000"))

        val expenses = (1..10000).map {
            Expense(
                title = "Expense $it",
                amount = 10.0,
                categoryId = categoryId,
                date = System.currentTimeMillis(),
                paidByMemberId = memberId,
                month = YearMonth.now().monthValue,
                year = YearMonth.now().year,
                splitType = SplitType.EQUAL
            )
        }

        val startTime = System.currentTimeMillis()
        
        db.runInTransaction {
            expenses.forEach { runBlocking { expenseDao.insert(it) } }
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Assert insertion takes less than 15 seconds for 10k items in transaction
        assert(duration < 15000) { "Insertion took too long: $duration ms" }
        
        // Read test
        val readStartTime = System.currentTimeMillis()
        val retrieved = expenseDao.getExpensesByMonth(YearMonth.now().monthValue, YearMonth.now().year).first()
        val readEndTime = System.currentTimeMillis()
        
        assert(retrieved.size == 10000)
        assert((readEndTime - readStartTime) < 5000) { "Read took too long: ${readEndTime - readStartTime} ms" }
    }
}
