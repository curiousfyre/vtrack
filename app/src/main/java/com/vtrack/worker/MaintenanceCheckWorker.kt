package com.vtrack.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.vtrack.data.repository.FuelRepository
import com.vtrack.data.repository.MaintenanceRepository
import com.vtrack.data.repository.VehicleRepository
import com.vtrack.util.MaintenanceDueCalculator
import com.vtrack.util.MaintenanceUrgency
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MaintenanceCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val vehicleRepository: VehicleRepository,
    private val fuelRepository: FuelRepository,
    private val maintenanceRepository: MaintenanceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val vehicles = vehicleRepository.getAllActiveList()

        for (vehicle in vehicles) {
            val currentOdometer = fuelRepository.getCurrentOdometer(vehicle.id)
            val types = maintenanceRepository.getAllActiveTypesForVehicle(vehicle.id)

            for (type in types) {
                val lastRecord = maintenanceRepository.getLatestRecordForType(type.id)
                val status = MaintenanceDueCalculator.calculate(type, lastRecord, currentOdometer)
                val notificationId = (vehicle.id * 10000 + type.id).toInt()

                when (status.urgency) {
                    MaintenanceUrgency.OVERDUE -> NotificationHelper.fireNotification(
                        applicationContext, notificationId,
                        "Maintenance Overdue \u2014 ${vehicle.name}",
                        "${type.name} is overdue by ${-status.milesUntilDue} miles"
                    )
                    MaintenanceUrgency.DUE_SOON -> NotificationHelper.fireNotification(
                        applicationContext, notificationId,
                        "Maintenance Due Soon \u2014 ${vehicle.name}",
                        "${type.name} due in ${status.milesUntilDue} miles"
                    )
                    MaintenanceUrgency.OK -> { /* no notification */ }
                }
            }
        }
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<MaintenanceCheckWorker>(
                24, TimeUnit.HOURS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "maintenance_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
