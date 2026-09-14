package com.fpf.blucon.data


import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fpf.blucon.data.devices.DeviceNameDao
import com.fpf.blucon.data.devices.DeviceNameEntity
import com.fpf.blucon.data.devices.clusters.ClusterCrossRefDao
import com.fpf.blucon.data.devices.clusters.ClusterCrossRefEntity
import com.fpf.blucon.data.devices.clusters.DeviceClusterDao
import com.fpf.blucon.data.devices.clusters.DeviceClusterEntity
import com.fpf.blucon.data.scans.ScanDao
import com.fpf.blucon.data.scans.ScanEntity
import com.fpf.blucon.data.scans.ScanEntryDao
import com.fpf.blucon.data.scans.ScanEntryEntity

@Database(
    entities = [
        ScanEntity::class,
        ScanEntryEntity::class,
        DeviceNameEntity::class,
        DeviceClusterEntity::class,
        ClusterCrossRefEntity::class
    ],
    version = 1 ,
    exportSchema = false
)
abstract class ScanDatabase : RoomDatabase() {

    abstract fun scanEntryDao(): ScanEntryDao
    abstract fun scanDao(): ScanDao

    abstract fun deviceNameDao(): DeviceNameDao
    abstract fun deviceClusterDao(): DeviceClusterDao
    abstract fun clusterCrossRefDao(): ClusterCrossRefDao

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
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}