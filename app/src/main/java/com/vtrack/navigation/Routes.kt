package com.vtrack.navigation

sealed class Route(val route: String) {
    object Dashboard : Route("dashboard")
    object FuelList : Route("fuel_list")
    object FuelEntry : Route("fuel_entry/{vehicleId}?entryId={entryId}") {
        fun create(vehicleId: Long, entryId: Long? = null): String {
            val base = "fuel_entry/$vehicleId"
            return if (entryId != null) "$base?entryId=$entryId" else base
        }
    }
    object MaintenanceTypes : Route("maintenance_types")
    object MaintenanceTypeForm : Route("maintenance_type_form/{vehicleId}?typeId={typeId}") {
        fun create(vehicleId: Long, typeId: Long? = null): String {
            val base = "maintenance_type_form/$vehicleId"
            return if (typeId != null) "$base?typeId=$typeId" else base
        }
    }
    object MaintenanceHistory : Route("maintenance_history/{typeId}") {
        fun create(typeId: Long): String = "maintenance_history/$typeId"
    }
    object LogMaintenance : Route("log_maintenance/{vehicleId}?typeId={typeId}") {
        fun create(vehicleId: Long, typeId: Long? = null): String {
            val base = "log_maintenance/$vehicleId"
            return if (typeId != null) "$base?typeId=$typeId" else base
        }
    }
    object VehicleList : Route("vehicle_list")
    object VehicleForm : Route("vehicle_form?vehicleId={vehicleId}") {
        fun create(vehicleId: Long? = null): String {
            return if (vehicleId != null) "vehicle_form?vehicleId=$vehicleId" else "vehicle_form"
        }
    }
    object Stats : Route("stats")
    object Settings : Route("settings")
}
