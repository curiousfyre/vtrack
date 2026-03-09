package com.vtrack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [
        ForeignKey(
            entity = MaintenanceType::class,
            parentColumns = ["id"],
            childColumns = ["maintenanceTypeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("maintenanceTypeId"),
        Index("vehicleId"),
        Index("vehicleId", "maintenanceTypeId", "odometer")
    ]
)
data class MaintenanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val maintenanceTypeId: Long,
    val vehicleId: Long,
    val date: Long,
    val odometer: Int,
    val cost: Double? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
