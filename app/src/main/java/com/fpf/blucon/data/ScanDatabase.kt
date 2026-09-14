package com.fpf.blucon.data


import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fpf.blucon.data.devices.DeviceNameDao
import com.fpf.blucon.data.devices.DeviceNameEntity
import com.fpf.blucon.data.migrations.MIGRATION_1_2
import com.fpf.blucon.data.scans.ScanDao
import com.fpf.blucon.data.scans.ScanEntity
import com.fpf.blucon.data.scans.ScanEntryDao
import com.fpf.blucon.data.scans.ScanEntryEntity

@Database(
    entities = [
        ScanEntity::class,
        ScanEntryEntity::class,
        DeviceNameEntity::class
    ],
    version = 2 ,
    exportSchema = false
)
abstract class ScanDatabase : RoomDatabase() {

    abstract fun scanEntryDao(): ScanEntryDao
    abstract fun scanDao(): ScanDao

    abstract fun deviceNameDao(): DeviceNameDao

    companion object {
        @Volatile
        private var INSTANCE: ScanDatabase? = null
        const val DB_NAME = "scan_database"

        const val TAG = "ScanDatabase"

        fun close() {
            INSTANCE?.close()
            INSTANCE = null
        }

        fun getDatabase(application: Application): ScanDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    application,
                    ScanDatabase::class.java,
                    DB_NAME
                ).setJournalMode(JournalMode.TRUNCATE)
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}