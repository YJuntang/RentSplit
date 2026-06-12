package com.rentsplit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = Household::class,
            parentColumns = ["id"],
            childColumns = ["householdId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("householdId")]
)
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val householdId: Long,
    val name: String,
    val colorHex: String,
    val isHouseLeader: Boolean = false
)
