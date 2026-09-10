package com.fpf.blucon.ui.components.bluetooth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fpf.blucon.bluetooth.BTDevice
import com.fpf.blucon.bluetooth.toBluetoothSigUuid

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun DeviceRow(
    device: BTDevice,
    onGetCompanyName: (manufacturerId: Int) -> String?,
    onGetServiceName: (serviceId: Int) -> String?,
) {
    var expanded by remember { mutableStateOf(false) }

    val knownServices = device.serviceUuids.mapNotNull { uuid ->
        onGetServiceName(uuid.toBluetoothSigUuid())
    }

    val knownServiceData = device.serviceData.mapNotNull { (uuid, data) ->
        if (data.isEmpty()) return@mapNotNull null
        onGetServiceName(uuid.toBluetoothSigUuid())
            ?.let { "$it: ${data.toHexString()}" }
    }

    val manufacturer = device.manufacturerData.entries.firstOrNull()?.let { (id, _) ->
        onGetCompanyName(id) ?: id.toString()
    }

    val hasMoreDetails =
        device.txPower != null ||
                manufacturer != null ||
                knownServices.isNotEmpty() ||
                knownServiceData.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unknown device",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${device.rssi} dBm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!hasMoreDetails) {
                    Text(
                        text = "No more details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    device.txPower?.let {
                        InfoRow("TX Power", "$it dBm")
                    }

                    manufacturer?.let {
                        InfoRow("Manufacturer", it)
                    }

                    if (knownServices.isNotEmpty()) {
                        InfoSection("Services", knownServices)
                    }

                    if (knownServiceData.isNotEmpty()) {
                        InfoSection("Service Data", knownServiceData)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun InfoSection(title: String, values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        values.forEach {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
