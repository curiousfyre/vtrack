package com.vtrack.data.repository

import com.vtrack.data.db.dao.VehicleDao
import com.vtrack.data.model.Vehicle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao
) {

    fun getAllActive(): Flow<List<Vehicle>> = vehicleDao.getAllActive()

    suspend fun getById(id: Long): Vehicle? = vehicleDao.getById(id)

    suspend fun getAllActiveList(): List<Vehicle> = vehicleDao.getAllActiveList()

    suspend fun insert(vehicle: Vehicle): Long = vehicleDao.insert(vehicle)

    suspend fun update(vehicle: Vehicle) = vehicleDao.update(vehicle)

    suspend fun delete(vehicle: Vehicle) = vehicleDao.delete(vehicle)
}
