package com.vtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vtrack.data.model.FuelEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelEntryDao {

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer DESC")
    fun getAllForVehicle(vehicleId: Long): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE id = :id")
    suspend fun getById(id: Long): FuelEntry?

    @Query("SELECT MAX(odometer) FROM fuel_entries WHERE vehicleId = :vehicleId")
    suspend fun getLatestOdometer(vehicleId: Long): Int?

    @Query(
        "SELECT * FROM fuel_entries " +
        "WHERE vehicleId = :vehicleId AND isPartialFill = 0 AND odometer < :currentOdometer " +
        "ORDER BY odometer DESC LIMIT 1"
    )
    suspend fun getPreviousFullFill(vehicleId: Long, currentOdometer: Int): FuelEntry?

    @Query(
        "SELECT SUM(gallons) FROM fuel_entries " +
        "WHERE vehicleId = :vehicleId AND odometer > (" +
            "SELECT COALESCE(MAX(odometer), 0) FROM fuel_entries " +
            "WHERE vehicleId = :vehicleId AND isPartialFill = 0 AND odometer < :currentOdometer" +
        ") AND odometer <= :currentOdometer"
    )
    suspend fun getGallonsSinceLastFullFill(vehicleId: Long, currentOdometer: Int): Double?

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer DESC")
    suspend fun getAllForVehicleList(vehicleId: Long): List<FuelEntry>

    @Insert
    suspend fun insert(entry: FuelEntry): Long

    @Update
    suspend fun update(entry: FuelEntry)

    @Delete
    suspend fun delete(entry: FuelEntry)
}
