package com.rentsplit.ui.home

import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Member
import com.rentsplit.data.model.SplitType
import java.time.LocalDate

data class MemberSplitState(
    val member: Member,
    val amount: String = "0.00",
    val isManuallyEdited: Boolean = false
)

data class AddExpenseUiState(
    val expenseId: Long? = null,
    val title: String = "",
    val amount: String = "",
    val categories: List<Category> = emptyList(),
    val categoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val paidByMemberId: Long? = null,
    val members: List<Member> = emptyList(),
    val splitType: SplitType = SplitType.EQUAL,
    val memberSplits: List<MemberSplitState> = emptyList(),
    val isSaving: Boolean = false,
    val isScanningReceipt: Boolean = false,
    val uiError: String? = null,
    val success: Boolean = false
)
