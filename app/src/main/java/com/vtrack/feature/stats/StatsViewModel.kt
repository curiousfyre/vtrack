package com.vtrack.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.FuelEntry
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class StatsUiState(
    val mpgData: List<Pair<Long, Double>> = emptyList(),
    val costPerMonthData: List<Pair<String, Double>> = emptyList(),
    val averageMpg: Double? = null,
    val bestMpg: Double? = null,
    val worstMpg: Double? = null,
    val totalGallons: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalMiles: Int = 0,
    val costPerMile: Double? = null,
    val fillUpCount: Int = 0,
    val isLoading: Boolean = true,
    val activeVehicleId: Long? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val fuelRepository: FuelRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    val uiState: StateFlow<StatsUiState> = preferencesManager.activeVehicleId
        .flatMapLatest { vehicleId ->
            if (vehicleId == null) {
                flowOf(StatsUiState(isLoading = false))
            } else {
                fuelRepository.getAllForVehicle(vehicleId).map { entries ->
                    buildStats(entries, vehicleId)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    private fun buildStats(entries: List<FuelEntry>, vehicleId: Long): StatsUiState {
        if (entries.isEmpty()) {
            return StatsUiState(
                isLoading = false,
                activeVehicleId = vehicleId
            )
        }

        val sortedByOdometer = entries.sortedBy { it.odometer }

        // Calculate MPG for each entry
        val mpgPairs = mutableListOf<Pair<Long, Double>>()
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
                    if (mpg != null) {
                        mpgPairs.add(Pair(entry.date, mpg))
                    }
                }
            }
        }

        val mpgValues = mpgPairs.map { it.second }
        val averageMpg = MpgCalculator.calculateAverageMpg(mpgValues)
        val bestMpg = mpgValues.maxOrNull()
        val worstMpg = mpgValues.minOrNull()

        val totalGallons = entries.sumOf { it.gallons }
        val totalSpent = entries.sumOf { it.totalCost }
        val totalMiles = if (sortedByOdometer.size >= 2) {
            sortedByOdometer.last().odometer - sortedByOdometer.first().odometer
        } else {
            0
        }
        val costPerMile = MpgCalculator.calculateCostPerMile(totalSpent, totalMiles)

        // Group costs by month
        val costByMonth = entries
            .groupBy { monthFormat.format(Date(it.date)) }
            .map { (month, monthEntries) ->
                Pair(month, monthEntries.sumOf { it.totalCost })
            }
            .sortedBy { pair ->
                entries.filter { monthFormat.format(Date(it.date)) == pair.first }
                    .minOf { it.date }
            }

        return StatsUiState(
            mpgData = mpgPairs.sortedBy { it.first },
            costPerMonthData = costByMonth,
            averageMpg = averageMpg,
            bestMpg = bestMpg,
            worstMpg = worstMpg,
            totalGallons = totalGallons,
            totalSpent = totalSpent,
            totalMiles = totalMiles,
            costPerMile = costPerMile,
            fillUpCount = entries.size,
            isLoading = false,
            activeVehicleId = vehicleId
        )
    }
}
