package com.vtrack.feature.fuel.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.FuelEntry
import com.vtrack.data.model.FuelEntryWithMpg
import com.vtrack.data.repository.FuelRepository
import com.vtrack.util.MpgCalculator
import com.vtrack.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FuelListUiState(
    val entries: List<FuelEntryWithMpg> = emptyList(),
    val activeVehicleId: Long? = null,
    val averageMpg: Double? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FuelListViewModel @Inject constructor(
    private val fuelRepository: FuelRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<FuelListUiState> = preferencesManager.activeVehicleId
        .flatMapLatest { vehicleId ->
            if (vehicleId == null) {
                flowOf(FuelListUiState(isLoading = false))
            } else {
                fuelRepository.getAllForVehicle(vehicleId).map { entries ->
                    val entriesWithMpg = calculateMpgForEntries(entries)
                    val mpgValues = entriesWithMpg.mapNotNull { it.mpg }
                    FuelListUiState(
                        entries = entriesWithMpg,
                        activeVehicleId = vehicleId,
                        averageMpg = MpgCalculator.calculateAverageMpg(mpgValues),
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FuelListUiState())

    private fun calculateMpgForEntries(entries: List<FuelEntry>): List<FuelEntryWithMpg> {
        val sorted = entries.sortedBy { it.odometer }
        return sorted.mapIndexed { index, entry ->
            val mpg = if (index == 0) {
                null
            } else {
                val previousFullFill = sorted.take(index).lastOrNull { !it.isPartialFill }
                if (previousFullFill != null && !entry.isPartialFill) {
                    val gallonsBetween = sorted
                        .filter { it.odometer > previousFullFill.odometer && it.odometer <= entry.odometer }
                        .sumOf { it.gallons }
                    MpgCalculator.calculateMpg(
                        entry.odometer,
                        previousFullFill.odometer,
                        gallonsBetween,
                        entry.isPartialFill
                    )
                } else {
                    null
                }
            }
            FuelEntryWithMpg(entry, mpg)
        }.sortedByDescending { it.entry.odometer }
    }

    fun deleteEntry(entry: FuelEntry) {
        viewModelScope.launch { fuelRepository.delete(entry) }
    }

    fun restoreEntry(entry: FuelEntry) {
        viewModelScope.launch { fuelRepository.insert(entry) }
    }
}
