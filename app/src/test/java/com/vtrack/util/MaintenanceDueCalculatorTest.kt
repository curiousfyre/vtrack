package com.vtrack.util

import com.google.common.truth.Truth.assertThat
import com.vtrack.data.model.MaintenanceRecord
import com.vtrack.data.model.MaintenanceType
import org.junit.Test

class MaintenanceDueCalculatorTest {

    private val oilChangeType = MaintenanceType(
        id = 1,
        vehicleId = 1,
        name = "Oil Change",
        intervalMiles = 5000
    )

    private fun makeRecord(odometer: Int, date: Long = 1000L) = MaintenanceRecord(
        id = 1,
        maintenanceTypeId = 1,
        vehicleId = 1,
        date = date,
        odometer = odometer
    )

    @Test
    fun `returns OK when below 90 percent of interval`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = makeRecord(odometer = 50000),
            currentOdometer = 52000
        )
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OK)
        assertThat(status.milesSinceService).isEqualTo(2000)
        assertThat(status.milesUntilDue).isEqualTo(3000)
        assertThat(status.percentUsed).isWithin(0.001).of(0.4)
    }

    @Test
    fun `returns DUE_SOON at exactly 90 percent`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = makeRecord(odometer = 50000),
            currentOdometer = 54500
        )
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.DUE_SOON)
        assertThat(status.percentUsed).isWithin(0.001).of(0.9)
    }

    @Test
    fun `returns DUE_SOON at 95 percent`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = makeRecord(odometer = 50000),
            currentOdometer = 54750
        )
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.DUE_SOON)
        assertThat(status.milesUntilDue).isEqualTo(250)
    }

    @Test
    fun `returns OVERDUE at exactly 100 percent`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = makeRecord(odometer = 50000),
            currentOdometer = 55000
        )
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OVERDUE)
        assertThat(status.milesUntilDue).isEqualTo(0)
        assertThat(status.percentUsed).isWithin(0.001).of(1.0)
    }

    @Test
    fun `returns OVERDUE when past interval`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = makeRecord(odometer = 50000),
            currentOdometer = 56000
        )
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OVERDUE)
        assertThat(status.milesUntilDue).isEqualTo(-1000)
        assertThat(status.percentUsed).isWithin(0.001).of(1.2)
    }

    @Test
    fun `uses vehicleInitialOdometer when no last record exists`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = null,
            currentOdometer = 52000,
            vehicleInitialOdometer = 50000
        )
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OK)
        assertThat(status.milesSinceService).isEqualTo(2000)
        assertThat(status.lastServiceOdometer).isNull()
        assertThat(status.lastServiceDate).isNull()
    }

    @Test
    fun `uses zero as default when no last record and no initial odometer`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = null,
            currentOdometer = 3000
        )
        assertThat(status.milesSinceService).isEqualTo(3000)
        assertThat(status.milesUntilDue).isEqualTo(2000)
    }

    @Test
    fun `handles zero intervalMiles without crash`() {
        val zeroIntervalType = oilChangeType.copy(intervalMiles = 0)
        val status = MaintenanceDueCalculator.calculate(
            type = zeroIntervalType,
            lastRecord = makeRecord(odometer = 50000),
            currentOdometer = 50100
        )
        assertThat(status.percentUsed).isEqualTo(0.0)
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OK)
    }

    @Test
    fun `populates lastServiceOdometer and lastServiceDate from record`() {
        val record = makeRecord(odometer = 50000, date = 1719792000000L)
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = record,
            currentOdometer = 51000
        )
        assertThat(status.lastServiceOdometer).isEqualTo(50000)
        assertThat(status.lastServiceDate).isEqualTo(1719792000000L)
    }

    @Test
    fun `boundary just below 90 percent returns OK`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = makeRecord(odometer = 50000),
            currentOdometer = 54499
        )
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OK)
    }

    @Test
    fun `boundary just below 100 percent returns DUE_SOON`() {
        val status = MaintenanceDueCalculator.calculate(
            type = oilChangeType,
            lastRecord = makeRecord(odometer = 50000),
            currentOdometer = 54999
        )
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.DUE_SOON)
    }

    // nextDueOdometer override tests

    @Test
    fun `nextDueOdometer overrides interval when no last record`() {
        val type = oilChangeType.copy(nextDueOdometer = 95000)
        val status = MaintenanceDueCalculator.calculate(
            type = type,
            lastRecord = null,
            currentOdometer = 93000,
            vehicleInitialOdometer = 93000
        )
        assertThat(status.milesUntilDue).isEqualTo(2000)
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OK)
    }

    @Test
    fun `nextDueOdometer shows OVERDUE when past due`() {
        val type = oilChangeType.copy(nextDueOdometer = 95000)
        val status = MaintenanceDueCalculator.calculate(
            type = type,
            lastRecord = null,
            currentOdometer = 96000,
            vehicleInitialOdometer = 93000
        )
        assertThat(status.milesUntilDue).isEqualTo(-1000)
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OVERDUE)
    }

    @Test
    fun `nextDueOdometer ignored when last record is past override`() {
        val type = oilChangeType.copy(nextDueOdometer = 95000)
        val status = MaintenanceDueCalculator.calculate(
            type = type,
            lastRecord = makeRecord(odometer = 96000),
            currentOdometer = 98000
        )
        // Falls back to interval: 96000 + 5000 = 101000, so 3000 until due
        assertThat(status.milesUntilDue).isEqualTo(3000)
        assertThat(status.urgency).isEqualTo(MaintenanceUrgency.OK)
    }

    @Test
    fun `nextDueOdometer used when last record is before override`() {
        val type = oilChangeType.copy(nextDueOdometer = 95000)
        val status = MaintenanceDueCalculator.calculate(
            type = type,
            lastRecord = makeRecord(odometer = 90000),
            currentOdometer = 93000
        )
        assertThat(status.milesUntilDue).isEqualTo(2000)
        // percentUsed = 3000 / (95000 - 90000) = 0.6
        assertThat(status.percentUsed).isWithin(0.001).of(0.6)
    }
}
