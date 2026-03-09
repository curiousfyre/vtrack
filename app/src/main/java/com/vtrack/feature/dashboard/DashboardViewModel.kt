package com.vtrack.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.FuelEntry
import com.vtrack.data.model.Vehicle
import com.vtrack.data.repository.FuelRepository
import com.vtrack.data.repository.MaintenanceRepository
import com.vtrack.data.repository.VehicleRepository
import com.vtrack.util.MaintenanceDueCalculator
import com.vtrack.util.MaintenanceStatus
import com.vtrack.util.MaintenanceUrgency
import com.vtrack.util.MpgCalculator
import com.vtrack.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val vehicle: Vehicle? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val currentOdometer: Int = 0,
    val totalMiles: Int = 0,
    val lastFillUp: FuelEntry? = null,
    val lastMpg: Double? = null,
    val averageMpg: Double? = null,
    val totalFuelSpend: Double = 0.0,
    val upcomingMaintenance: List<MaintenanceStatus> = emptyList(),
    val isLoading: Boolean = true,
    val hasNoVehicle: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val fuelRepository: FuelRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        preferencesManager.activeVehicleId,
        vehicleRepository.getAllActive()
    ) { activeId, vehicles ->
        Pair(activeId, vehicles)
    }.flatMapLatest { (activeId, vehicles) ->
        if (vehicles.isEmpty()) {
            flowOf(DashboardUiState(isLoading = false, hasNoVehicle = true))
        } else {
            val vehicleId = activeId ?: vehicles.first().id
            val vehicle = vehicles.find { it.id == vehicleId } ?: vehicles.first()

            fuelRepository.getAllForVehicle(vehicle.id).flatMapLatest { entries ->
                val currentOdometer = fuelRepository.getCurrentOdometer(vehicle.id)
                val totalMiles = currentOdometer - vehicle.initialOdometer
                val sortedEntries = entries.sortedByDescending { it.date }
                val lastFillUp = sortedEntries.firstOrNull()

                val sortedByOdometer = entries.sortedBy { it.odometer }
                val mpgValues = calculateAllMpg(sortedByOdometer)
                val averageMpg = MpgCalculator.calculateAverageMpg(mpgValues)

                val lastMpg = if (lastFillUp != null && !lastFillUp.isPartialFill) {
                    calculateLastMpg(sortedByOdometer, lastFillUp)
                } else {
                    null
                }

                val totalFuelSpend = entries.sumOf { it.totalCost }

                val maintenanceStatuses = calculateMaintenanceStatuses(vehicle.id, currentOdometer, vehicle.initialOdometer)

                flowOf(
                    DashboardUiState(
                        vehicle = vehicle,
                        vehicles = vehicles,
                        currentOdometer = currentOdometer,
                        totalMiles = totalMiles,
                        lastFillUp = lastFillUp,
                        lastMpg = lastMpg,
                        averageMpg = averageMpg,
                        totalFuelSpend = totalFuelSpend,
                        upcomingMaintenance = maintenanceStatuses,
                        isLoading = false,
                        hasNoVehicle = false
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    private fun calculateAllMpg(sortedByOdometer: List<FuelEntry>): List<Double> {
        val mpgValues = mutableListOf<Double>()
        sortedByOdometer.forEachIndexed { index, entry ->
            if (index > 0 && !entry.isPartialFill) {
                val previousFullFill = sortedByOdometer.take(index).lastOrNull { !it.isPartialFill }
                if (previousFullFill != null) {
                    val gallonsBetween = sortedByOdometer
                        .filter { it.odometer > previousFullFill.odometer && it.odometer <= entry.odometer }
                        .sumOf { it.gallons }
                    val mpg = MpgCalculator.calculateMpg(
                        entry.odometer,
                        previousFullFill.odometer,
                        gallonsBetween,
                        entry.isPartialFill
                    )
                    if (mpg != null) mpgValues.add(mpg)
                }
            }
        }
        return mpgValues
    }

    private fun calculateLastMpg(sortedByOdometer: List<FuelEntry>, lastEntry: FuelEntry): Double? {
        val index = sortedByOdometer.indexOf(lastEntry)
        if (index <= 0) return null
        val previousFullFill = sortedByOdometer.take(index).lastOrNull { !it.isPartialFill } ?: return null
        val gallonsBetween = sortedByOdometer
            .filter { it.odometer > previousFullFill.odometer && it.odometer <= lastEntry.odometer }
            .sumOf { it.gallons }
        return MpgCalculator.calculateMpg(
            lastEntry.odometer,
            previousFullFill.odometer,
            gallonsBetween,
            lastEntry.isPartialFill
        )
    }

    private suspend fun calculateMaintenanceStatuses(
        vehicleId: Long,
        currentOdometer: Int,
        vehicleInitialOdometer: Int
    ): List<MaintenanceStatus> {
        val types = maintenanceRepository.getAllActiveTypesForVehicle(vehicleId)
        return types.map { type ->
            val lastRecord = maintenanceRepository.getLatestRecordForType(type.id)
            MaintenanceDueCalculator.calculate(type, lastRecord, currentOdometer, vehicleInitialOdometer)
        }.filter { it.urgency != MaintenanceUrgency.OK }
            .sortedWith(compareByDescending<MaintenanceStatus> {
                when (it.urgency) {
                    MaintenanceUrgency.OVERDUE -> 2
                    MaintenanceUrgency.DUE_SOON -> 1
                    MaintenanceUrgency.OK -> 0
                }
            }.thenBy { it.milesUntilDue })
    }

    fun switchVehicle(vehicleId: Long) {
        viewModelScope.launch {
            preferencesManager.setActiveVehicleId(vehicleId)
        }
    }
}
