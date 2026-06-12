package com.rentsplit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rent_config",
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
data class RentConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val householdId: Long,
    val amount: Double,
    val dueDayOfMonth: Int,
    val lastGeneratedMonth: Int = 0,
    val lastGeneratedYear: Int = 0
)
