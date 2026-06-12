package com.rentsplit.util

import com.rentsplit.data.model.Member
import com.rentsplit.ui.history.ExpenseDetail
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ExportUtils {
    fun generateCsv(expenses: List<ExpenseDetail>, members: List<Member>): String {
        val header = mutableListOf("Date", "Title", "Category", "Total Amount", "Paid By")
        members.forEach { header.add("${it.name}'s Share") }
        
        val sb = StringBuilder()
        sb.append(header.joinToString(",")).append("\n")
        
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
        
        expenses.forEach { detail ->
            val row = mutableListOf<String>()
            row.add(dateFormatter.format(Instant.ofEpochMilli(detail.expense.date)))
            row.add(detail.expense.title.escapeCsv())
            row.add((detail.category?.name ?: "Other").escapeCsv())
            row.add("%.2f".format(detail.expense.amount))
            row.add(detail.paidByMember?.name?.escapeCsv() ?: "Unknown")
            
            members.forEach { member ->
                val split = detail.splits.find { it.memberId == member.id }
                row.add("%.2f".format(split?.amountOwed ?: 0.0))
            }
            
            sb.append(row.joinToString(",")).append("\n")
        }
        
        return sb.toString()
    }
    
    private fun String.escapeCsv(): String {
        return if (this.contains(",") || this.contains("\"") || this.contains("\n")) {
            "\"${this.replace("\"", "\"\"")}\""
        } else {
            this
        }
    }
}
