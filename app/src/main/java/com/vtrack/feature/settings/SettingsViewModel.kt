package com.vtrack.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vtrack.data.model.FuelEntry
import com.vtrack.data.repository.FuelRepository
import com.vtrack.util.FormatUtil
import com.vtrack.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val exportCsvContent: String? = null,
    val activeVehicleId: Long? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val fuelRepository: FuelRepository
) : ViewModel() {

    private val csvContent = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.notificationsEnabled,
        preferencesManager.activeVehicleId,
        csvContent
    ) { notificationsEnabled, activeVehicleId, csv ->
        SettingsUiState(
            notificationsEnabled = notificationsEnabled,
            exportCsvContent = csv,
            activeVehicleId = activeVehicleId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val vehicleId = uiState.value.activeVehicleId ?: return@launch
            val entries = fuelRepository.getAllForVehicleList(vehicleId)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            val csv = buildString {
                appendLine("Date,Odometer,Gallons,Price/Gallon,Total Cost,Partial Fill,Notes")
                entries.sortedByDescending { it.date }.forEach { entry ->
                    val date = dateFormat.format(Date(entry.date))
                    val notes = entry.notes?.replace(",", ";")?.replace("\n", " ") ?: ""
                    appendLine("$date,${entry.odometer},${entry.gallons},${entry.pricePerGallon},${entry.totalCost},${entry.isPartialFill},\"$notes\"")
                }
            }
            csvContent.value = csv
        }
    }

    fun clearExportData() {
        csvContent.value = null
    }
}
