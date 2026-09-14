package com.fpf.blucon.location

import android.content.res.Resources

object PostcodeCsvLoader {
    fun load(resources: Resources, resId: Int): Map<String, Pair<Double, Double>> {
        return resources.openRawResource(resId).bufferedReader().useLines{ lines ->
            lines.drop(1)
                .mapNotNull { line ->
                    val columns = line.split(",")

                    if (columns.size < 3) return@mapNotNull null

                    val postcode = columns[0].trim().removeSurrounding("\"")
                    val longitude = columns[1].trim().toDoubleOrNull() ?: return@mapNotNull null
                    val latitude = columns[2].trim().toDoubleOrNull() ?: return@mapNotNull null

                    postcode to Pair(latitude, longitude)
                }
                .toMap()
        }
    }
}