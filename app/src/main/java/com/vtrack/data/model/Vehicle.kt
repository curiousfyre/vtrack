package com.vtrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val initialOdometer: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
