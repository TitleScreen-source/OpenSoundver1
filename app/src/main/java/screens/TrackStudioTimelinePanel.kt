package com.opensound.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensound.app.editor.isProtectedLayer
import com.opensound.app.editor.moveLayerTime
import com.opensound.app.editor.snapLayerToPlayhead
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType

@Composable
internal fun TimelinePanel(
    layers: List<AtmosphereLayer>,
    previewTimeSeconds: Float,
    onPreviewTimeChange: (Float) -> Unit,
    selectedLayerId: String,
    onLayerTimelineEdit: (AtmosphereLayer) -> Unit,
    onAddLayer: (AtmosphereLayerType) -> Unit,
    onDuplicateLayer: () -> Unit,
    onDeleteLayer: () -> Unit,
    onToggleLayerVisibility: (AtmosphereLayer) -> Unit,
    onLayerSelected: (AtmosphereLayer) -> Unit
) {
    val selectedLayer = layers.firstOrNull { it.id == selectedLayerId }
    var showAddLayerTypes by remember {
        mutableStateOf(false)
    }

    StudioPanel(title = "Atmosphere timeline") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimelineActionButton(
                label = if (showAddLayerTypes) "Cancel" else "+ Layer",
                modifier = Modifier.weight(1f),
                onClick = { showAddLayerTypes = !showAddLayerTypes }
            )
            TimelineActionButton(
                label = "Duplicate",
                modifier = Modifier.weight(1f),
                onClick = onDuplicateLayer
            )
            TimelineActionButton(
                label = "Delete",
                modifier = Modifier.weight(1f),
                enabled = selectedLayer?.id?.let { !isProtectedLayer(it) } == true,
                onClick = onDeleteLayer
            )
        }

        if (showAddLayerTypes) {
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(AtmosphereLayerType.values().toList()) { type ->
                    SectionChip(
                        name = layerTypeLabel(type),
                        selected = false,
                        onClick = {
                            onAddLayer(type)
                            showAddLayerTypes = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Playhead: ${formatTimelineTime(previewTimeSeconds)}" +
                if (selectedLayer != null) " - ${selectedLayer.name}" else "",
            color = Color(0xFFA9A1B6)
        )

        Slider(
            value = previewTimeSeconds,
            onValueChange = onPreviewTimeChange,
            valueRange = 0f..100f
        )

        selectedLayer?.let { layer ->
            Spacer(modifier = Modifier.height(12.dp))

            TimelineSelectedClipInfo(layer = layer)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Fine tune",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    TimelineActionButton(
                        label = "Left 1s",
                        modifier = Modifier.width(82.dp),
                        onClick = { onLayerTimelineEdit(moveLayerTime(layer, -1f)) }
                    )
                }
                item {
                    TimelineActionButton(
                        label = "Right 1s",
                        modifier = Modifier.width(90.dp),
                        onClick = { onLayerTimelineEdit(moveLayerTime(layer, 1f)) }
                    )
                }
                item {
                    TimelineActionButton(
                        label = "Snap",
                        modifier = Modifier.width(72.dp),
                        onClick = {
                            onLayerTimelineEdit(
                                snapLayerToPlayhead(
                                    layer = layer,
                                    playheadSeconds = previewTimeSeconds
                                )
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TimelineRuler()

        Spacer(modifier = Modifier.height(6.dp))

        layers.forEach { layer ->
            TimelineLayerRow(
                layer = layer,
                previewTimeSeconds = previewTimeSeconds,
                isSelected = selectedLayerId == layer.id,
                onToggleVisibility = { onToggleLayerVisibility(layer) },
                onClipMove = { onLayerTimelineEdit(it) },
                onClick = { onLayerSelected(layer) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
