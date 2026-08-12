package com.vtrack.feature.vehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.Vehicle
import com.vtrack.data.repository.VehicleRepository
import com.vtrack.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehicleFormUiState(
    val name: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val initialOdometer: String = "",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VehicleFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle["vehicleId"] ?: -1L
    private val _uiState = MutableStateFlow(VehicleFormUiState())
    val uiState: StateFlow<VehicleFormUiState> = _uiState.asStateFlow()

    init {
        if (vehicleId > 0) {
            viewModelScope.launch {
                vehicleRepository.getById(vehicleId)?.let { vehicle ->
                    _uiState.update {
                        it.copy(
                            name = vehicle.name,
                            make = vehicle.make,
                            model = vehicle.model,
                            year = vehicle.year.toString(),
                            initialOdometer = vehicle.initialOdometer?.toString() ?: "",
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun updateName(value: String) { _uiState.update { it.copy(name = value) } }
    fun updateMake(value: String) { _uiState.update { it.copy(make = value) } }
    fun updateModel(value: String) { _uiState.update { it.copy(model = value) } }
    fun updateYear(value: String) { _uiState.update { it.copy(year = value) } }
    fun updateInitialOdometer(value: String) { _uiState.update { it.copy(initialOdometer = value) } }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    fun save() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        if (state.make.isBlank()) {
            _uiState.update { it.copy(error = "Make is required") }
            return
        }
        if (state.model.isBlank()) {
            _uiState.update { it.copy(error = "Model is required") }
            return
        }
        val year = state.year.toIntOrNull()
        if (year == null || year < 1900 || year > 2100) {
            _uiState.update { it.copy(error = "Enter a valid year") }
            return
        }
        val odometer = if (state.initialOdometer.isBlank()) null else {
            val parsed = state.initialOdometer.toIntOrNull()
            if (parsed == null || parsed < 0) {
                _uiState.update { it.copy(error = "Enter a valid odometer reading or leave blank") }
                return
            }
            parsed
        }

        viewModelScope.launch {
            if (vehicleId > 0) {
                vehicleRepository.update(
                    Vehicle(
                        id = vehicleId,
                        name = state.name.trim(),
                        make = state.make.trim(),
                        model = state.model.trim(),
                        year = year,
                        initialOdometer = odometer
                    )
                )
            } else {
                val newId = vehicleRepository.insert(
                    Vehicle(
                        name = state.name.trim(),
                        make = state.make.trim(),
                        model = state.model.trim(),
                        year = year,
                        initialOdometer = odometer
                    )
                )
                preferencesManager.setActiveVehicleId(newId)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
