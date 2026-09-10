package com.fpf.blucon.data.scans

import androidx.room.Embedded

data class ScanWithCount (
    @Embedded val scan: ScanEntity,
    val count: Int
)
