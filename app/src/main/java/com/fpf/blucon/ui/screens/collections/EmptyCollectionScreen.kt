package com.fpf.blucon.ui.screens.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun EmptyCollectionScreen(
    isVisible: Boolean,
    ) {
    if (!isVisible) return

    val message = "Devices with similar names are grouped together automatically"
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Devices,
                contentDescription = "Devices icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            )

            Text(
                text = "No device collections",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
