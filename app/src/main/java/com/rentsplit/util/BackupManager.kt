package com.rentsplit.util

import com.rentsplit.data.model.*
import com.rentsplit.data.repository.RentSplitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val repository: RentSplitRepository
) {
    suspend fun exportDbToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        // 1. Households
        val householdsJson = JSONArray()
        repository.getAllHouseholdsSync().forEach { h ->
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            householdsJson.put(obj)
        }
        root.put("households", householdsJson)

        // 2. Members
        val membersJson = JSONArray()
        repository.getAllMembersSync().forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("householdId", m.householdId)
            obj.put("name", m.name)
            obj.put("colorHex", m.colorHex)
            membersJson.put(obj)
        }
        root.put("members", membersJson)

        // 3. Categories
        val categoriesJson = JSONArray()
        repository.getAllCategoriesSync().forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("householdId", c.householdId)
            obj.put("name", c.name)
            obj.put("iconName", c.iconName)
            obj.put("colorHex", c.colorHex)
            obj.put("budgetLimit", c.budgetLimit ?: JSONObject.NULL)
            obj.put("sortOrder", c.sortOrder)
            categoriesJson.put(obj)
        }
        root.put("categories", categoriesJson)

        // 4. Expenses
        val expensesJson = JSONArray()
        repository.getAllExpensesSync().forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("title", e.title)
            obj.put("amount", e.amount)
            obj.put("categoryId", e.categoryId ?: JSONObject.NULL)
            obj.put("date", e.date)
            obj.put("paidByMemberId", e.paidByMemberId ?: JSONObject.NULL)
            obj.put("month", e.month)
            obj.put("year", e.year)
            obj.put("splitType", e.splitType.name)
            expensesJson.put(obj)
        }
        root.put("expenses", expensesJson)

        // 5. Splits
        val splitsJson = JSONArray()
        repository.getAllSplitsSync().forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("expenseId", s.expenseId)
            obj.put("memberId", s.memberId)
            obj.put("amountOwed", s.amountOwed)
            obj.put("amountPaid", s.amountPaid)
            obj.put("isPaid", s.isPaid)
            splitsJson.put(obj)
        }
        root.put("splits", splitsJson)

        root.toString(2)
    }

    suspend fun importDbFromJson(jsonString: String) = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonString)

        val households = mutableListOf<Household>()
        val householdsJson = root.optJSONArray("households") ?: JSONArray()
        for (i in 0 until householdsJson.length()) {
            val obj = householdsJson.getJSONObject(i)
            households.add(
                Household(
                    id = obj.getLong("id"),
                    name = obj.getString("name")
                )
            )
        }

        val members = mutableListOf<Member>()
        val membersJson = root.optJSONArray("members") ?: JSONArray()
        for (i in 0 until membersJson.length()) {
            val obj = membersJson.getJSONObject(i)
            members.add(
                Member(
                    id = obj.getLong("id"),
                    householdId = obj.getLong("householdId"),
                    name = obj.getString("name"),
                    colorHex = obj.getString("colorHex")
                )
            )
        }

        val categories = mutableListOf<Category>()
        val categoriesJson = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until categoriesJson.length()) {
            val obj = categoriesJson.getJSONObject(i)
            categories.add(
                Category(
                    id = obj.getLong("id"),
                    householdId = obj.getLong("householdId"),
                    name = obj.getString("name"),
                    iconName = obj.getString("iconName"),
                    colorHex = obj.getString("colorHex"),
                    budgetLimit = if (obj.isNull("budgetLimit")) null else obj.getDouble("budgetLimit"),
                    sortOrder = obj.getInt("sortOrder")
                )
            )
        }

        val expenses = mutableListOf<Expense>()
        val expensesJson = root.optJSONArray("expenses") ?: JSONArray()
        for (i in 0 until expensesJson.length()) {
            val obj = expensesJson.getJSONObject(i)
            expenses.add(
                Expense(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    amount = obj.getDouble("amount"),
                    categoryId = if (obj.isNull("categoryId")) null else obj.getLong("categoryId"),
                    date = obj.getLong("date"),
                    paidByMemberId = if (obj.isNull("paidByMemberId")) null else obj.getLong("paidByMemberId"),
                    month = obj.getInt("month"),
                    year = obj.getInt("year"),
                    splitType = SplitType.valueOf(obj.getString("splitType"))
                )
            )
        }

        val splits = mutableListOf<Split>()
        val splitsJson = root.optJSONArray("splits") ?: JSONArray()
        for (i in 0 until splitsJson.length()) {
            val obj = splitsJson.getJSONObject(i)
            splits.add(
                Split(
                    id = obj.getLong("id"),
                    expenseId = obj.getLong("expenseId"),
                    memberId = obj.getLong("memberId"),
                    amountOwed = obj.getDouble("amountOwed"),
                    amountPaid = obj.getDouble("amountPaid"),
                    isPaid = obj.getBoolean("isPaid")
                )
            )
        }

        repository.restoreFullDatabase(households, members, categories, expenses, splits)
    }
}
