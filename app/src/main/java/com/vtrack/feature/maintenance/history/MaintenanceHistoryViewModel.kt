package com.vtrack.feature.maintenance.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.MaintenanceRecord
import com.vtrack.data.repository.FuelRepository
import com.vtrack.data.repository.MaintenanceRepository
import com.vtrack.data.repository.VehicleRepository
import com.vtrack.util.MaintenanceDueCalculator
import com.vtrack.util.MaintenanceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenanceHistoryUiState(
    val typeName: String = "",
    val vehicleId: Long = -1L,
    val typeId: Long = -1L,
    val status: MaintenanceStatus? = null,
    val records: List<MaintenanceRecord> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MaintenanceHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val maintenanceRepository: MaintenanceRepository,
    private val fuelRepository: FuelRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val typeId: Long = savedStateHandle["typeId"] ?: -1L

    private val _uiState = MutableStateFlow(MaintenanceHistoryUiState())
    val uiState: StateFlow<MaintenanceHistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val type = maintenanceRepository.getTypeById(typeId)
            if (type == null) {
                _uiState.value = MaintenanceHistoryUiState(isLoading = false)
                return@launch
            }

            val currentOdometer = fuelRepository.getCurrentOdometer(type.vehicleId)
            val vehicle = vehicleRepository.getById(type.vehicleId)
            val initialOdometer = vehicle?.initialOdometer ?: 0
            val lastRecord = maintenanceRepository.getLatestRecordForType(typeId)
            val status = MaintenanceDueCalculator.calculate(type, lastRecord, currentOdometer, initialOdometer)

            _uiState.value = MaintenanceHistoryUiState(
                typeName = type.name,
                vehicleId = type.vehicleId,
                typeId = typeId,
                status = status,
                isLoading = false
            )
        }

        viewModelScope.launch {
            maintenanceRepository.getAllRecordsForType(typeId).collect { records ->
                _uiState.value = _uiState.value.copy(
                    records = records.sortedByDescending { it.date }
                )
                // Recalculate status when records change
                val type = maintenanceRepository.getTypeById(typeId) ?: return@collect
                val currentOdometer = fuelRepository.getCurrentOdometer(type.vehicleId)
                val vehicleForCalc = vehicleRepository.getById(type.vehicleId)
                val initOdo = vehicleForCalc?.initialOdometer ?: 0
                val lastRecord = maintenanceRepository.getLatestRecordForType(typeId)
                val status = MaintenanceDueCalculator.calculate(type, lastRecord, currentOdometer, initOdo)
                _uiState.value = _uiState.value.copy(status = status)
            }
        }
    }
}
