package com.vtrack.di

import android.content.Context
import androidx.room.Room
import com.vtrack.data.local.AppDatabase
import com.vtrack.data.local.dao.FuelEntryDao
import com.vtrack.data.local.dao.MaintenanceRecordDao
import com.vtrack.data.local.dao.MaintenanceTypeDao
import com.vtrack.data.local.dao.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vtrack_database"
        ).build()
    }

    @Provides
    fun provideVehicleDao(db: AppDatabase): VehicleDao {
        return db.vehicleDao()
    }

    @Provides
    fun provideFuelEntryDao(db: AppDatabase): FuelEntryDao {
        return db.fuelEntryDao()
    }

    @Provides
    fun provideMaintenanceTypeDao(db: AppDatabase): MaintenanceTypeDao {
        return db.maintenanceTypeDao()
    }

    @Provides
    fun provideMaintenanceRecordDao(db: AppDatabase): MaintenanceRecordDao {
        return db.maintenanceRecordDao()
    }
}
