package com.vtrack.data.repository

import com.vtrack.data.db.dao.MaintenanceRecordDao
import com.vtrack.data.db.dao.MaintenanceTypeDao
import com.vtrack.data.model.MaintenanceRecord
import com.vtrack.data.model.MaintenanceType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepository @Inject constructor(
    private val maintenanceTypeDao: MaintenanceTypeDao,
    private val maintenanceRecordDao: MaintenanceRecordDao
) {

    // Maintenance Types

    fun getAllTypesForVehicle(vehicleId: Long): Flow<List<MaintenanceType>> =
        maintenanceTypeDao.getAllForVehicle(vehicleId)

    suspend fun getTypeById(id: Long): MaintenanceType? =
        maintenanceTypeDao.getById(id)

    suspend fun getAllActiveTypesForVehicle(vehicleId: Long): List<MaintenanceType> =
        maintenanceTypeDao.getAllActiveForVehicle(vehicleId)

    suspend fun insertType(type: MaintenanceType): Long =
        maintenanceTypeDao.insert(type)

    suspend fun updateType(type: MaintenanceType) =
        maintenanceTypeDao.update(type)

    suspend fun deleteType(type: MaintenanceType) =
        maintenanceTypeDao.delete(type)

    // Maintenance Records

    fun getAllRecordsForVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> =
        maintenanceRecordDao.getAllForVehicle(vehicleId)

    fun getAllRecordsForType(typeId: Long): Flow<List<MaintenanceRecord>> =
        maintenanceRecordDao.getAllForType(typeId)

    suspend fun getLatestRecordForType(typeId: Long): MaintenanceRecord? =
        maintenanceRecordDao.getLatestForType(typeId)

    suspend fun insertRecord(record: MaintenanceRecord): Long =
        maintenanceRecordDao.insert(record)

    suspend fun updateRecord(record: MaintenanceRecord) =
        maintenanceRecordDao.update(record)

    suspend fun deleteRecord(record: MaintenanceRecord) =
        maintenanceRecordDao.delete(record)
}
