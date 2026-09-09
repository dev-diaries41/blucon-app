package com.fpf.blucon.bluetooth

import android.content.Context
import org.yaml.snakeyaml.Yaml

object BluetoothDocsYamlParser {
    fun parseCompanyIdentifiers(context: Context, resId: Int): Map<Int, String> {
        val yaml = context.resources.openRawResource(resId).bufferedReader().readText()
        val data = Yaml().load<Map<String, Any>>(yaml)
        val identifiers = data["company_identifiers"] as? List<Map<String, Any>> ?: return emptyMap()

        return identifiers.associate {
            val value = it["value"] as Int
            val name = it["name"] as String
            value to name
        }
    }

    fun parseServiceUuids(context: Context, resId: Int): Map<Int, String> {
        val yaml =context.resources.openRawResource(resId).bufferedReader().readText()
        val data = Yaml().load<Map<String, Any>>(yaml)
        val identifiers = data["uuids"] as? List<Map<String, Any>> ?: return emptyMap()

        return identifiers.associate {
            val uuid = it["uuid"] as Int
            val name = it["name"] as String
            uuid to name
        }
    }
}