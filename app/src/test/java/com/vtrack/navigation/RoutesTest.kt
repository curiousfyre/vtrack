package com.vtrack.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RoutesTest {

    @Test
    fun `FuelEntry create with entryId includes query param`() {
        val route = Route.FuelEntry.create(vehicleId = 5, entryId = 10)
        assertThat(route).isEqualTo("fuel_entry/5?entryId=10")
    }

    @Test
    fun `FuelEntry create without entryId omits query param`() {
        val route = Route.FuelEntry.create(vehicleId = 5)
        assertThat(route).isEqualTo("fuel_entry/5")
    }

    @Test
    fun `VehicleForm create with vehicleId includes query param`() {
        val route = Route.VehicleForm.create(vehicleId = 3)
        assertThat(route).isEqualTo("vehicle_form?vehicleId=3")
    }

    @Test
    fun `VehicleForm create without vehicleId returns base route`() {
        val route = Route.VehicleForm.create()
        assertThat(route).isEqualTo("vehicle_form")
    }

    @Test
    fun `MaintenanceHistory create produces correct URI`() {
        val route = Route.MaintenanceHistory.create(typeId = 7)
        assertThat(route).isEqualTo("maintenance_history/7")
    }

    @Test
    fun `MaintenanceTypeForm create with typeId includes query param`() {
        val route = Route.MaintenanceTypeForm.create(vehicleId = 2, typeId = 4)
        assertThat(route).isEqualTo("maintenance_type_form/2?typeId=4")
    }

    @Test
    fun `MaintenanceTypeForm create without typeId omits query param`() {
        val route = Route.MaintenanceTypeForm.create(vehicleId = 2)
        assertThat(route).isEqualTo("maintenance_type_form/2")
    }

    @Test
    fun `LogMaintenance create with typeId includes query param`() {
        val route = Route.LogMaintenance.create(vehicleId = 1, typeId = 3)
        assertThat(route).isEqualTo("log_maintenance/1?typeId=3")
    }

    @Test
    fun `LogMaintenance create without typeId omits query param`() {
        val route = Route.LogMaintenance.create(vehicleId = 1)
        assertThat(route).isEqualTo("log_maintenance/1")
    }

    @Test
    fun `static routes have expected paths`() {
        assertThat(Route.Dashboard.route).isEqualTo("dashboard")
        assertThat(Route.FuelList.route).isEqualTo("fuel_list")
        assertThat(Route.MaintenanceTypes.route).isEqualTo("maintenance_types")
        assertThat(Route.VehicleList.route).isEqualTo("vehicle_list")
        assertThat(Route.Stats.route).isEqualTo("stats")
        assertThat(Route.Settings.route).isEqualTo("settings")
    }
}
