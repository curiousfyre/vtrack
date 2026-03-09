package com.vtrack.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vtrack.data.db.dao.FuelEntryDao
import com.vtrack.data.db.dao.MaintenanceRecordDao
import com.vtrack.data.db.dao.MaintenanceTypeDao
import com.vtrack.data.db.dao.VehicleDao
import com.vtrack.data.model.FuelEntry
import com.vtrack.data.model.MaintenanceRecord
import com.vtrack.data.model.MaintenanceType
import com.vtrack.data.model.Vehicle

@Database(
    entities = [Vehicle::class, FuelEntry::class, MaintenanceType::class, MaintenanceRecord::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun maintenanceTypeDao(): MaintenanceTypeDao
    abstract fun maintenanceRecordDao(): MaintenanceRecordDao
}
