package com.vtrack.feature.maintenance.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.MaintenanceType
import com.vtrack.data.repository.FuelRepository
import com.vtrack.data.repository.MaintenanceRepository
import com.vtrack.util.MaintenanceDueCalculator
import com.vtrack.util.MaintenanceStatus
import com.vtrack.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenanceTypesUiState(
    val statuses: List<MaintenanceStatus> = emptyList(),
    val activeVehicleId: Long? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MaintenanceTypesViewModel @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val fuelRepository: FuelRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<MaintenanceTypesUiState> = preferencesManager.activeVehicleId
        .flatMapLatest { vehicleId ->
            if (vehicleId == null) {
                flowOf(MaintenanceTypesUiState(isLoading = false))
            } else {
                maintenanceRepository.getAllTypesForVehicle(vehicleId).map { types ->
                    val currentOdometer = fuelRepository.getCurrentOdometer(vehicleId)
                    val statuses = types.map { type ->
                        val lastRecord = maintenanceRepository.getLatestRecordForType(type.id)
                        MaintenanceDueCalculator.calculate(type, lastRecord, currentOdometer)
                    }
                    MaintenanceTypesUiState(
                        statuses = statuses,
                        activeVehicleId = vehicleId,
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MaintenanceTypesUiState())

    fun deleteType(type: MaintenanceType) {
        viewModelScope.launch {
            maintenanceRepository.deleteType(type)
        }
    }
}
