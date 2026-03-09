package com.vtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vtrack.data.model.MaintenanceType
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceTypeDao {

    @Query("SELECT * FROM maintenance_types WHERE vehicleId = :vehicleId AND isActive = 1")
    fun getAllForVehicle(vehicleId: Long): Flow<List<MaintenanceType>>

    @Query("SELECT * FROM maintenance_types WHERE id = :id")
    suspend fun getById(id: Long): MaintenanceType?

    @Query("SELECT * FROM maintenance_types WHERE vehicleId = :vehicleId AND isActive = 1")
    suspend fun getAllActiveForVehicle(vehicleId: Long): List<MaintenanceType>

    @Insert
    suspend fun insert(type: MaintenanceType): Long

    @Update
    suspend fun update(type: MaintenanceType)

    @Delete
    suspend fun delete(type: MaintenanceType)
}
