package com.fpf.blucon.ui.components.placeholders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyItemsScreen(
    isVisible: Boolean,
    title: String? = null,
    description: String? = null,
) {
    if (!isVisible) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = title?: "No items",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium
            )

            description?.let{
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
