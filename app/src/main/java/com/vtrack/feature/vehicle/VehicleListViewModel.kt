package com.vtrack.feature.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.Vehicle
import com.vtrack.data.repository.VehicleRepository
import com.vtrack.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehicleListUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val activeVehicleId: Long? = null
)

@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<VehicleListUiState> = combine(
        vehicleRepository.getAllActive(),
        preferencesManager.activeVehicleId
    ) { vehicles, activeId ->
        VehicleListUiState(vehicles = vehicles, activeVehicleId = activeId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleListUiState())

    fun setActiveVehicle(id: Long) {
        viewModelScope.launch { preferencesManager.setActiveVehicleId(id) }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch { vehicleRepository.delete(vehicle) }
    }
}
