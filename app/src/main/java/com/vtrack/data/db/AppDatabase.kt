package com.vtrack.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun maintenanceTypeDao(): MaintenanceTypeDao
    abstract fun maintenanceRecordDao(): MaintenanceRecordDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE vehicles_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "make TEXT NOT NULL, " +
                    "model TEXT NOT NULL, " +
                    "year INTEGER NOT NULL, " +
                    "initialOdometer INTEGER, " +
                    "isActive INTEGER NOT NULL DEFAULT 1, " +
                    "createdAt INTEGER NOT NULL" +
                    ")"
                )
                db.execSQL("INSERT INTO vehicles_new SELECT * FROM vehicles")
                db.execSQL("DROP TABLE vehicles")
                db.execSQL("ALTER TABLE vehicles_new RENAME TO vehicles")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE maintenance_types ADD COLUMN nextDueOdometer INTEGER DEFAULT NULL")
            }
        }
    }
}
