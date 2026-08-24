package com.vtrack.util

import com.vtrack.data.model.MaintenanceType
import com.vtrack.data.model.MaintenanceRecord

enum class MaintenanceUrgency { OK, DUE_SOON, OVERDUE }

data class MaintenanceStatus(
    val type: MaintenanceType,
    val lastServiceOdometer: Int?,
    val lastServiceDate: Long?,
    val currentOdometer: Int,
    val milesSinceService: Int,
    val milesUntilDue: Int,
    val percentUsed: Double,
    val urgency: MaintenanceUrgency
)

object MaintenanceDueCalculator {
    fun calculate(
        type: MaintenanceType,
        lastRecord: MaintenanceRecord?,
        currentOdometer: Int,
        vehicleInitialOdometer: Int = 0
    ): MaintenanceStatus {
        val baseOdometer = lastRecord?.odometer ?: vehicleInitialOdometer
        val milesSince = currentOdometer - baseOdometer

        // Use nextDueOdometer override when set and not yet superseded by a service record
        val useOverride = type.nextDueOdometer != null &&
            (lastRecord == null || lastRecord.odometer < type.nextDueOdometer)
        val nextDue = if (useOverride) type.nextDueOdometer!! else baseOdometer + type.intervalMiles
        val milesUntilDue = nextDue - currentOdometer
        val effectiveInterval = nextDue - baseOdometer
        val percentUsed = if (effectiveInterval > 0) milesSince.toDouble() / effectiveInterval else 0.0

        val urgency = when {
            percentUsed >= 1.0 -> MaintenanceUrgency.OVERDUE
            percentUsed >= 0.9 -> MaintenanceUrgency.DUE_SOON
            else -> MaintenanceUrgency.OK
        }

        return MaintenanceStatus(
            type = type,
            lastServiceOdometer = lastRecord?.odometer,
            lastServiceDate = lastRecord?.date,
            currentOdometer = currentOdometer,
            milesSinceService = milesSince,
            milesUntilDue = milesUntilDue,
            percentUsed = percentUsed,
            urgency = urgency
        )
    }
}
