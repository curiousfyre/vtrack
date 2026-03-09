package com.vtrack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_entries",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("vehicleId"),
        Index("vehicleId", "odometer"),
        Index("vehicleId", "date")
    ]
)
data class FuelEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val date: Long,
    val odometer: Int,
    val gallons: Double,
    val pricePerGallon: Double,
    val totalCost: Double,
    val isPartialFill: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
