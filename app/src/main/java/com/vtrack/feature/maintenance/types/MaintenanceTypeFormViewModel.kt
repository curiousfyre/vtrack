package com.vtrack.feature.maintenance.types

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.MaintenanceType
import com.vtrack.data.repository.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenancePreset(
    val name: String,
    val intervalMiles: Int
)

val MAINTENANCE_PRESETS = listOf(
    MaintenancePreset("Oil Change", 5000),
    MaintenancePreset("Tire Rotation", 7500),
    MaintenancePreset("Air Filter", 15000),
    MaintenancePreset("Brake Inspection", 20000),
    MaintenancePreset("Transmission Fluid", 30000),
    MaintenancePreset("Spark Plugs", 30000),
    MaintenancePreset("Coolant Flush", 30000),
    MaintenancePreset("Timing Belt", 60000)
)

data class MaintenanceTypeFormUiState(
    val name: String = "",
    val intervalMiles: String = "",
    val intervalMonths: String = "",
    val description: String = "",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val selectedPresetIndex: Int? = null
)

@HiltViewModel
class MaintenanceTypeFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle["vehicleId"] ?: -1L
    private val typeId: Long = savedStateHandle["typeId"] ?: -1L

    private val _uiState = MutableStateFlow(MaintenanceTypeFormUiState())
    val uiState: StateFlow<MaintenanceTypeFormUiState> = _uiState.asStateFlow()

    init {
        if (typeId > 0) {
            viewModelScope.launch {
                maintenanceRepository.getTypeById(typeId)?.let { type ->
                    _uiState.update {
                        it.copy(
                            name = type.name,
                            intervalMiles = type.intervalMiles.toString(),
                            intervalMonths = type.intervalMonths?.toString() ?: "",
                            description = type.description ?: "",
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun selectPreset(index: Int) {
        val preset = MAINTENANCE_PRESETS[index]
        _uiState.update {
            it.copy(
                name = preset.name,
                intervalMiles = preset.intervalMiles.toString(),
                selectedPresetIndex = index
            )
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, selectedPresetIndex = null) }
    }

    fun updateIntervalMiles(value: String) {
        _uiState.update { it.copy(intervalMiles = value) }
    }

    fun updateIntervalMonths(value: String) {
        _uiState.update { it.copy(intervalMonths = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun save() {
        val state = _uiState.value
        val name = state.name.trim()
        val intervalMiles = state.intervalMiles.toIntOrNull()
        val intervalMonths = state.intervalMonths.toIntOrNull()

        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        if (intervalMiles == null || intervalMiles <= 0) {
            _uiState.update { it.copy(error = "Enter a valid mileage interval") }
            return
        }

        viewModelScope.launch {
            val type = MaintenanceType(
                id = if (typeId > 0) typeId else 0,
                vehicleId = vehicleId,
                name = name,
                intervalMiles = intervalMiles,
                intervalMonths = intervalMonths,
                description = state.description.ifBlank { null }
            )
            if (typeId > 0) {
                maintenanceRepository.updateType(type)
            } else {
                maintenanceRepository.insertType(type)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
