package com.fpf.blucon.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE device_cluster_crossref_new (
                deviceId INTEGER NOT NULL,
                clusterId INTEGER NOT NULL,
                PRIMARY KEY(deviceId),
                FOREIGN KEY(clusterId)
                    REFERENCES device_cluster(clusterId)
                    ON DELETE CASCADE,
                FOREIGN KEY(deviceId)
                    REFERENCES device_name(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            INSERT OR IGNORE INTO device_cluster_crossref_new (deviceId, clusterId)
            SELECT deviceId, clusterId
            FROM device_cluster_crossref
            """.trimIndent()
        )

        database.execSQL("DROP TABLE device_cluster_crossref")

        database.execSQL(
            """
            ALTER TABLE device_cluster_crossref_new
            RENAME TO device_cluster_crossref
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX index_device_cluster_crossref_clusterId
            ON device_cluster_crossref(clusterId)
            """.trimIndent()
        )
    }
}