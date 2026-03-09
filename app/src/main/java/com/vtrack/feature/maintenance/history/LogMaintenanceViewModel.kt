package com.vtrack.feature.maintenance.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.MaintenanceRecord
import com.vtrack.data.repository.FuelRepository
import com.vtrack.data.repository.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogMaintenanceUiState(
    val typeName: String = "",
    val date: Long = System.currentTimeMillis(),
    val odometer: String = "",
    val cost: String = "",
    val notes: String = "",
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LogMaintenanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val maintenanceRepository: MaintenanceRepository,
    private val fuelRepository: FuelRepository
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle["vehicleId"] ?: -1L
    private val typeId: Long = savedStateHandle["typeId"] ?: -1L

    private val _uiState = MutableStateFlow(LogMaintenanceUiState())
    val uiState: StateFlow<LogMaintenanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val type = maintenanceRepository.getTypeById(typeId)
            val currentOdometer = fuelRepository.getCurrentOdometer(vehicleId)
            _uiState.update {
                it.copy(
                    typeName = type?.name ?: "",
                    odometer = if (currentOdometer > 0) currentOdometer.toString() else ""
                )
            }
        }
    }

    fun updateDate(value: Long) {
        _uiState.update { it.copy(date = value) }
    }

    fun updateOdometer(value: String) {
        _uiState.update { it.copy(odometer = value) }
    }

    fun updateCost(value: String) {
        _uiState.update { it.copy(cost = value) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun save() {
        val state = _uiState.value
        val odometer = state.odometer.toIntOrNull()
        val cost = state.cost.toDoubleOrNull()

        if (odometer == null || odometer <= 0) {
            _uiState.update { it.copy(error = "Enter a valid odometer reading") }
            return
        }

        viewModelScope.launch {
            val record = MaintenanceRecord(
                maintenanceTypeId = typeId,
                vehicleId = vehicleId,
                date = state.date,
                odometer = odometer,
                cost = cost,
                notes = state.notes.ifBlank { null }
            )
            maintenanceRepository.insertRecord(record)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
