package com.vtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vtrack.data.model.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceRecordDao {

    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getAllForVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records WHERE maintenanceTypeId = :typeId ORDER BY date DESC")
    fun getAllForType(typeId: Long): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records WHERE maintenanceTypeId = :typeId ORDER BY odometer DESC LIMIT 1")
    suspend fun getLatestForType(typeId: Long): MaintenanceRecord?

    @Insert
    suspend fun insert(record: MaintenanceRecord): Long

    @Update
    suspend fun update(record: MaintenanceRecord)

    @Delete
    suspend fun delete(record: MaintenanceRecord)
}
