package com.rentsplit.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.rentsplit.data.model.Household
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.RentConfig
import com.rentsplit.data.preferences.UserPreferencesRepository
import com.rentsplit.data.repository.RentSplitRepository
import com.rentsplit.util.BackupManager
import com.rentsplit.worker.RentReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.rentsplit.util.SnackbarManager

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val repository: RentSplitRepository,
    private val backupManager: BackupManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val userPreferences = preferencesRepository.userPreferencesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    private val _currentHousehold = MutableStateFlow<Household?>(null)
    val currentHousehold = _currentHousehold.asStateFlow()

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members = _members.asStateFlow()

    private val _rentConfig = MutableStateFlow<RentConfig?>(null)
    val rentConfig = _rentConfig.asStateFlow()

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError = _uiError.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp = _isBackingUp.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring = _isRestoring.asStateFlow()

    private val _isCsvExporting = MutableStateFlow(false)
    val isCsvExporting = _isCsvExporting.asStateFlow()

    private val _isMarkdownExporting = MutableStateFlow(false)
    val isMarkdownExporting = _isMarkdownExporting.asStateFlow()

    init {
        loadHouseholdData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadHouseholdData() {
        viewModelScope.launch {
            try {
                repository.getAllHouseholds()
                    .flatMapLatest { households ->
                        val household = households.firstOrNull()
                        _currentHousehold.value = household
                        if (household != null) {
                            viewModelScope.launch {
                                repository.getRentConfig(household.id).collect {
                                    _rentConfig.value = it
                                }
                            }
                            repository.getMembersByHousehold(household.id)
                        } else {
                            flowOf(emptyList())
                        }
                    }
                    .collect { members ->
                        _members.value = members
                    }
            } catch (e: Exception) {
                _uiError.value = "Failed to load settings data"
            }
        }
    }

    fun updateHouseholdName(name: String) {
        viewModelScope.launch {
            try {
                _currentHousehold.value?.let {
                    repository.updateHousehold(it.copy(name = name))
                    SnackbarManager.showMessage("Household name updated successfully!")
                }
            } catch (e: Exception) {
                _uiError.value = "Failed to update household name"
            }
        }
    }

    fun addMember(name: String, colorHex: String) {
        viewModelScope.launch {
            try {
                _currentHousehold.value?.let {
                    repository.insertMember(Member(householdId = it.id, name = name, colorHex = colorHex))
                    SnackbarManager.showMessage("Member \"$name\" added successfully!")
                }
            } catch (e: Exception) {
                _uiError.value = "Failed to add member"
            }
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            try {
                repository.updateMember(member)
                SnackbarManager.showMessage("Member \"${member.name}\" updated successfully!")
            } catch (e: Exception) {
                _uiError.value = "Failed to update member"
            }
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            try {
                repository.deleteMember(member)
                SnackbarManager.showMessage("Member \"${member.name}\" deleted successfully!")
            } catch (e: Exception) {
                _uiError.value = "Failed to delete member"
            }
        }
    }

    fun setHouseLeader(memberId: Long) {
        viewModelScope.launch {
            val currentMembers = _members.value
            currentMembers.forEach { member ->
                if (member.id == memberId && !member.isHouseLeader) {
                    repository.updateMember(member.copy(isHouseLeader = true))
                } else if (member.id != memberId && member.isHouseLeader) {
                    repository.updateMember(member.copy(isHouseLeader = false))
                }
            }
            SnackbarManager.showMessage("House leader updated!")
        }
    }

    fun saveRentConfig(amount: Double, dueDay: Int) {
        viewModelScope.launch {
            val householdId = _currentHousehold.value?.id ?: return@launch
            val currentConfig = _rentConfig.value
            val config = currentConfig?.copy(amount = amount, dueDayOfMonth = dueDay)
                ?: RentConfig(householdId = householdId, amount = amount, dueDayOfMonth = dueDay)
            repository.insertOrUpdateRentConfig(config)
            SnackbarManager.showMessage("Rent configuration saved successfully!")
        }
    }

    fun updateAppTheme(theme: String) {
        viewModelScope.launch {
            preferencesRepository.updateAppTheme(theme)
        }
    }

    fun updateDefaultSplitType(splitType: String) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultSplitType(splitType)
        }
    }

    fun updateRentDueDay(day: Int) {
        viewModelScope.launch {
            preferencesRepository.updateRentDueDay(day)
            scheduleReminder(day, userPreferences.value?.notificationsEnabled ?: false)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationsEnabled(enabled)
            scheduleReminder(userPreferences.value?.rentDueDay ?: 1, enabled)
        }
    }

    fun clearError() {
        _uiError.value = null
    }

    private fun scheduleReminder(day: Int, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelAllWorkByTag("rent_reminder")
            return
        }

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.MONTH, 1)
        }

        val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<RentReminderWorker>(30, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("rent_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "rent_reminder_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun exportDatabaseToJson(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isBackingUp.value = true
            try {
                val json = backupManager.exportDbToJson()
                onSuccess(json)
            } catch (e: Exception) {
                _uiError.value = "Failed to export backup: ${e.message}"
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun restoreDatabaseFromJson(jsonString: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isRestoring.value = true
            try {
                backupManager.importDbFromJson(jsonString)
                loadHouseholdData() // Reload members and household after restore
                onSuccess()
                SnackbarManager.showMessage("Backup restored successfully!")
            } catch (e: Exception) {
                _uiError.value = "Failed to restore backup: ${e.message}"
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun exportAdvancedCsv(startDate: Long, endDate: Long, selectedMemberIds: List<Long>, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isCsvExporting.value = true
            try {
                val allExpenses = repository.getAllExpensesSync()
                val allSplits = repository.getAllSplitsSync()
                val allMembers = repository.getAllMembersSync()
                val allCategories = repository.getAllCategoriesSync()
                
                val filteredExpenses = allExpenses.filter { it.date in startDate..endDate }
                val expenseMap = filteredExpenses.associateBy { it.id }
                
                val memberMap = allMembers.associateBy { it.id }
                val categoryMap = allCategories.associateBy { it.id }

                val filteredSplits = allSplits.filter { 
                    expenseMap.containsKey(it.expenseId) && selectedMemberIds.contains(it.memberId)
                }

                val csvBuilder = StringBuilder()
                csvBuilder.append("Date,Expense,Category,Member,Amount Owed,Amount Paid,Status\n")

                for (split in filteredSplits) {
                    val expense = expenseMap[split.expenseId]
                    val member = memberMap[split.memberId]
                    val categoryName = expense?.categoryId?.let { categoryMap[it]?.name } ?: "Unknown"

                    if (expense != null && member != null) {
                        val calendar = Calendar.getInstance().apply { timeInMillis = expense.date }
                        val dateStr = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
                        
                        val title = expense.title.replace(",", " ")
                        val memberName = member.name.replace(",", " ")
                        val status = if (split.isPaid) "Paid" else "Unpaid"
                        
                        csvBuilder.append("${dateStr},${title},${categoryName},${memberName},${split.amountOwed},${split.amountPaid},${status}\n")
                    }
                }
                
                onSuccess(csvBuilder.toString())
            } catch (e: Exception) {
                _uiError.value = "Failed to export CSV: ${e.message}"
            } finally {
                _isCsvExporting.value = false
            }
        }
    }

    fun exportAdvancedMarkdown(startDate: Long, endDate: Long, selectedMemberIds: List<Long>, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isMarkdownExporting.value = true
            try {
                val allExpenses = repository.getAllExpensesSync()
                val allSplits = repository.getAllSplitsSync()
                val allMembers = repository.getAllMembersSync()
                val allCategories = repository.getAllCategoriesSync()

                val memberMap = allMembers.associateBy { it.id }
                val categoryMap = allCategories.associateBy { it.id }

                // 1. Filter expenses by date
                val dateFilteredExpenses = allExpenses.filter { it.date in startDate..endDate }

                // 2. Filter expenses by selected members (must have at least one split involving a selected member)
                val filteredExpenses = dateFilteredExpenses.filter { expense ->
                    val splitsForExpense = allSplits.filter { it.expenseId == expense.id }
                    splitsForExpense.any { selectedMemberIds.contains(it.memberId) }
                }

                // Map splits by expenseId for quick lookup
                val splitsByExpense = allSplits.groupBy { it.expenseId }

                // 3. Generate summary
                val totalAmount = filteredExpenses.sumOf { it.amount }
                
                val categoryBreakdown = filteredExpenses.groupBy { it.categoryId }
                    .map { (catId, expenses) ->
                        val catName = categoryMap[catId]?.name ?: "Unknown"
                        val catSum = expenses.sumOf { it.amount }
                        catName to catSum
                    }.sortedByDescending { it.second }

                val mdBuilder = StringBuilder()
                mdBuilder.append("# Expense Report\n\n")
                
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val startStr = if (startDate == 0L) "Beginning" else sdf.format(java.util.Date(startDate))
                val endStr = if (endDate == Long.MAX_VALUE) "Present" else sdf.format(java.util.Date(endDate))
                mdBuilder.append("Report Period: **$startStr** to **$endStr**\n\n")

                mdBuilder.append("## Summary\n\n")
                mdBuilder.append("- **Total Expenses:** RM ${String.format(Locale.US, "%.2f", totalAmount)}\n")
                mdBuilder.append("- **Total Count:** ${filteredExpenses.size} expense(s)\n\n")
                
                mdBuilder.append("### Category Breakdown\n\n")
                if (categoryBreakdown.isEmpty()) {
                    mdBuilder.append("*No data available.*\n\n")
                } else {
                    mdBuilder.append("| Category | Total Amount | Percentage |\n")
                    mdBuilder.append("| --- | --- | --- |\n")
                    for ((catName, sum) in categoryBreakdown) {
                        val pct = if (totalAmount > 0) (sum / totalAmount) * 100 else 0.0
                        mdBuilder.append("| $catName | RM ${String.format(Locale.US, "%.2f", sum)} | ${String.format(Locale.US, "%.1f", pct)}% |\n")
                    }
                    mdBuilder.append("\n")
                }

                // Group expenses by Month-Year
                val calendar = Calendar.getInstance()
                val monthYearFormat = java.text.SimpleDateFormat("MMMM yyyy", Locale.US)
                
                val expensesByMonth = filteredExpenses.groupBy { expense ->
                    calendar.timeInMillis = expense.date
                    monthYearFormat.format(calendar.time)
                }

                // Sort months chronologically by date of first expense
                val sortedMonths = expensesByMonth.keys.sortedBy { monthStr ->
                    expensesByMonth[monthStr]?.firstOrNull()?.date ?: 0L
                }

                mdBuilder.append("## Monthly Breakdown\n\n")
                if (sortedMonths.isEmpty()) {
                    mdBuilder.append("*No expenses recorded in the selected period.*\n\n")
                } else {
                    for (monthStr in sortedMonths) {
                        mdBuilder.append("### $monthStr\n\n")
                        mdBuilder.append("| Date | Title | Category | Amount | Splits | Notes |\n")
                        mdBuilder.append("| --- | --- | --- | --- | --- | --- |\n")
                        
                        val monthExpenses = expensesByMonth[monthStr]?.sortedBy { it.date } ?: emptyList()
                        for (expense in monthExpenses) {
                            calendar.timeInMillis = expense.date
                            val dateStr = "${calendar.get(Calendar.YEAR)}-${String.format(Locale.US, "%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format(Locale.US, "%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
                            
                            val categoryName = categoryMap[expense.categoryId]?.name ?: "Unknown"
                            val formattedAmount = "RM ${String.format(Locale.US, "%.2f", expense.amount)}"
                            
                            val splits = splitsByExpense[expense.id]?.filter { selectedMemberIds.contains(it.memberId) } ?: emptyList()
                            val splitsStr = splits.joinToString("<br>") { split ->
                                val mName = memberMap[split.memberId]?.name ?: "Unknown"
                                val status = if (split.isPaid) "Paid" else "Unpaid"
                                "$mName: RM ${String.format(Locale.US, "%.2f", split.amountOwed)} ($status)"
                            }

                            val escapedTitle = expense.title.replace("|", "\\|")
                            val escapedSplits = splitsStr.replace("|", "\\|")
                            
                            mdBuilder.append("| $dateStr | $escapedTitle | $categoryName | $formattedAmount | $escapedSplits |  |\n")
                        }
                        mdBuilder.append("\n")
                    }
                }

                onSuccess(mdBuilder.toString())
            } catch (e: Exception) {
                _uiError.value = "Failed to export Markdown: ${e.message}"
            } finally {
                _isMarkdownExporting.value = false
            }
        }
    }
}
