package com.fpf.blucon.bluetooth.device

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceCollection (
    val id: Long,
    val name: String,
    val size: Int,
    val snippet: List<String> = emptyList(),

    ): Parcelable {

    companion object {
        const val UNLABELLED_COLLECTION = "Unlabelled collection"
    }
}