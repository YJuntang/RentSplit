package com.rentsplit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "households")
data class Household(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
