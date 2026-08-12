package com.vtrack.data.repository

import com.vtrack.data.db.dao.FuelEntryDao
import com.vtrack.data.db.dao.VehicleDao
import com.vtrack.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FuelRepository @Inject constructor(
    private val fuelEntryDao: FuelEntryDao,
    private val vehicleDao: VehicleDao
) {

    fun getAllForVehicle(vehicleId: Long): Flow<List<FuelEntry>> =
        fuelEntryDao.getAllForVehicle(vehicleId)

    suspend fun getById(id: Long): FuelEntry? = fuelEntryDao.getById(id)

    suspend fun getCurrentOdometer(vehicleId: Long): Int {
        val latestFromFuel = fuelEntryDao.getLatestOdometer(vehicleId)
        if (latestFromFuel != null) return latestFromFuel
        val vehicle = vehicleDao.getById(vehicleId)
        return vehicle?.initialOdometer ?: 0
    }

    suspend fun getFirstEntryOdometer(vehicleId: Long): Int? =
        fuelEntryDao.getFirstOdometer(vehicleId)

    suspend fun getPreviousFullFill(vehicleId: Long, currentOdometer: Int): FuelEntry? =
        fuelEntryDao.getPreviousFullFill(vehicleId, currentOdometer)

    suspend fun getGallonsSinceLastFullFill(vehicleId: Long, currentOdometer: Int): Double? =
        fuelEntryDao.getGallonsSinceLastFullFill(vehicleId, currentOdometer)

    suspend fun getAllForVehicleList(vehicleId: Long): List<FuelEntry> =
        fuelEntryDao.getAllForVehicleList(vehicleId)

    suspend fun insert(entry: FuelEntry): Long = fuelEntryDao.insert(entry)

    suspend fun update(entry: FuelEntry) = fuelEntryDao.update(entry)

    suspend fun delete(entry: FuelEntry) = fuelEntryDao.delete(entry)
}
