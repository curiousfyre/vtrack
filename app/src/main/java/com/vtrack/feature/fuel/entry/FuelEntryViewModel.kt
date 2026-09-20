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

enum class PriceInputMode { PRICE_PER_GALLON, TOTAL_COST }

data class FuelEntryUiState(
    val date: Long = System.currentTimeMillis(),
    val odometer: String = "",
    val gallons: String = "",
    val pricePerGallon: String = "",
    val totalCost: String = "",
    val priceInputMode: PriceInputMode = PriceInputMode.PRICE_PER_GALLON,
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
        _uiState.update {
            val g = value.toDoubleOrNull()
            when (it.priceInputMode) {
                PriceInputMode.PRICE_PER_GALLON -> {
                    val p = it.pricePerGallon.toDoubleOrNull()
                    val total = if (g != null && p != null) roundCost(g * p).toString() else it.totalCost
                    it.copy(gallons = value, totalCost = total)
                }
                PriceInputMode.TOTAL_COST -> {
                    val t = it.totalCost.toDoubleOrNull()
                    val price = if (g != null && g > 0 && t != null) roundPrice(t / g).toString() else it.pricePerGallon
                    it.copy(gallons = value, pricePerGallon = price)
                }
            }
        }
    }

    fun updatePricePerGallon(value: String) {
        _uiState.update {
            val g = it.gallons.toDoubleOrNull()
            val p = value.toDoubleOrNull()
            val total = if (g != null && p != null) roundCost(g * p).toString() else it.totalCost
            it.copy(pricePerGallon = value, priceInputMode = PriceInputMode.PRICE_PER_GALLON, totalCost = total)
        }
    }

    fun updateTotalCost(value: String) {
        _uiState.update {
            val g = it.gallons.toDoubleOrNull()
            val t = value.toDoubleOrNull()
            val price = if (g != null && g > 0 && t != null) roundPrice(t / g).toString() else it.pricePerGallon
            it.copy(totalCost = value, priceInputMode = PriceInputMode.TOTAL_COST, pricePerGallon = price)
        }
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

    private fun roundCost(value: Double): Double = Math.round(value * 100.0) / 100.0

    private fun roundPrice(value: Double): Double = Math.round(value * 1000.0) / 1000.0

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
        if ((price == null || price <= 0) && (total == null || total <= 0)) {
            _uiState.update { it.copy(error = "Enter price per gallon or total cost") }
            return
        }

        val finalPrice = if (price != null && price > 0) price
            else roundPrice(total!! / gallons)
        val finalTotal = if (total != null && total > 0) total
            else roundCost(gallons * finalPrice)

        viewModelScope.launch {
            val entry = FuelEntry(
                id = if (entryId > 0) entryId else 0,
                vehicleId = vehicleId,
                date = state.date,
                odometer = odometer,
                gallons = gallons,
                pricePerGallon = finalPrice,
                totalCost = finalTotal,
                isPartialFill = state.isPartialFill,
                notes = state.notes.ifBlank { null }
            )
            if (entryId > 0) fuelRepository.update(entry) else fuelRepository.insert(entry)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
