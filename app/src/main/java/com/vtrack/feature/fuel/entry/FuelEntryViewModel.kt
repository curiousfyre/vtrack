package com.vtrack.feature.fuel.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.FuelEntry
import com.vtrack.data.repository.FuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FuelEntryUiState(
    val date: Long = System.currentTimeMillis(),
    val odometer: String = "",
    val gallons: String = "",
    val pricePerGallon: String = "",
    val totalCost: String = "",
    val isPartialFill: Boolean = false,
    val notes: String = "",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FuelEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val fuelRepository: FuelRepository
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle["vehicleId"] ?: -1L
    private val entryId: Long = savedStateHandle["entryId"] ?: -1L

    private val _uiState = MutableStateFlow(FuelEntryUiState())
    val uiState: StateFlow<FuelEntryUiState> = _uiState.asStateFlow()

    init {
        if (entryId > 0) {
            viewModelScope.launch {
                fuelRepository.getById(entryId)?.let { entry ->
                    _uiState.update {
                        it.copy(
                            date = entry.date,
                            odometer = entry.odometer.toString(),
                            gallons = entry.gallons.toString(),
                            pricePerGallon = entry.pricePerGallon.toString(),
                            totalCost = entry.totalCost.toString(),
                            isPartialFill = entry.isPartialFill,
                            notes = entry.notes ?: "",
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    fun updateDate(value: Long) {
        _uiState.update { it.copy(date = value) }
    }

    fun updateOdometer(value: String) {
        _uiState.update { it.copy(odometer = value) }
    }

    fun updateGallons(value: String) {
        _uiState.update { it.copy(gallons = value) }
        autoCalculateTotalCost()
    }

    fun updatePricePerGallon(value: String) {
        _uiState.update { it.copy(pricePerGallon = value) }
        autoCalculateTotalCost()
    }

    fun updateTotalCost(value: String) {
        _uiState.update { it.copy(totalCost = value) }
    }

    fun updateIsPartialFill(value: Boolean) {
        _uiState.update { it.copy(isPartialFill = value) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun autoCalculateTotalCost() {
        val state = _uiState.value
        val g = state.gallons.toDoubleOrNull()
        val p = state.pricePerGallon.toDoubleOrNull()
        if (g != null && p != null) {
            val total = Math.round(g * p * 100.0) / 100.0
            _uiState.update { it.copy(totalCost = total.toString()) }
        }
    }

    fun save() {
        val state = _uiState.value
        val odometer = state.odometer.toIntOrNull()
        val gallons = state.gallons.toDoubleOrNull()
        val price = state.pricePerGallon.toDoubleOrNull()
        val total = state.totalCost.toDoubleOrNull()

        if (odometer == null || odometer <= 0) {
            _uiState.update { it.copy(error = "Enter a valid odometer reading") }
            return
        }
        if (gallons == null || gallons <= 0) {
            _uiState.update { it.copy(error = "Enter gallons filled") }
            return
        }
        if (price == null || price <= 0) {
            _uiState.update { it.copy(error = "Enter price per gallon") }
            return
        }

        val finalTotal = total ?: (Math.round(gallons * price * 100.0) / 100.0)

        viewModelScope.launch {
            val entry = FuelEntry(
                id = if (entryId > 0) entryId else 0,
                vehicleId = vehicleId,
                date = state.date,
                odometer = odometer,
                gallons = gallons,
                pricePerGallon = price,
                totalCost = finalTotal,
                isPartialFill = state.isPartialFill,
                notes = state.notes.ifBlank { null }
            )
            if (entryId > 0) fuelRepository.update(entry) else fuelRepository.insert(entry)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
