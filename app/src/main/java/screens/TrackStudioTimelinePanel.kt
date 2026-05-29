package com.opensound.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensound.app.editor.TIMELINE_DURATION_SECONDS
import com.opensound.app.editor.isProtectedLayer
import com.opensound.app.editor.layerDuration
import com.opensound.app.editor.moveLayerTime
import com.opensound.app.editor.moveLayerToStart
import com.opensound.app.editor.snapLayerToPlayhead
import com.opensound.app.editor.trimLayerEndTo
import com.opensound.app.editor.trimLayerStartTo
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType
import kotlin.math.roundToInt

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

@Composable
private fun TimelineActionButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = if (enabled) 0.07f else 0.03f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (enabled) 0.1f else 0.05f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.38f),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun TimelineSelectedClipInfo(layer: AtmosphereLayer) {
    val accentColor = colorForLayer(layer.type)
    val durationSeconds = layerDuration(layer)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.34f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = layer.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = layerTypeLabel(layer.type),
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimelineMetric(
                label = "Start",
                value = formatTimelineTime(layer.startTime),
                modifier = Modifier.weight(1f)
            )
            TimelineMetric(
                label = "End",
                value = formatTimelineTime(layer.endTime),
                modifier = Modifier.weight(1f)
            )
            TimelineMetric(
                label = "Length",
                value = "${durationSeconds.roundToInt()}s",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimelineMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFA9A1B6),
            maxLines = 1
        )
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun TimelineRuler() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(134.dp))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(0f, 25f, 50f, 75f, 100f).forEach { seconds ->
                Text(
                    text = formatTimelineTime(seconds),
                    color = Color(0xFF777083),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TimelineLayerRow(
    layer: AtmosphereLayer,
    previewTimeSeconds: Float,
    isSelected: Boolean,
    onToggleVisibility: () -> Unit,
    onClipMove: (AtmosphereLayer) -> Unit,
    onClick: () -> Unit
) {
    val accentColor = colorForLayer(layer.type)
    val duration = TIMELINE_DURATION_SECONDS
    val clipStart = layer.startTime.coerceIn(0f, duration)
    val minClipEnd = (clipStart + 1f).coerceAtMost(duration)
    val clipEnd = layer.endTime.coerceIn(clipStart, duration).coerceAtLeast(minClipEnd)
    val playhead = previewTimeSeconds.coerceIn(0f, duration)
    val density = LocalDensity.current
    val latestLayerState = rememberUpdatedState(layer)
    val rowShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(accentColor.copy(alpha = if (isSelected) 0.1f else 0f))
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor.copy(alpha = 0.34f) else Color.Transparent,
                shape = rowShape
            )
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = if (layer.isVisible) 0.08f else 0.03f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (layer.isVisible) 0.12f else 0.05f),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onToggleVisibility() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (layer.isVisible) "ON" else "OFF",
                color = Color.White.copy(alpha = if (layer.isVisible) 0.9f else 0.45f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${layerTypeShort(layer.type)} ${layer.name}",
            color = Color(0xFFC8BED8).copy(alpha = if (layer.isVisible) 1f else 0.42f),
            maxLines = 1,
            modifier = Modifier.width(80.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(30.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            val trackWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)

            Row(modifier = Modifier.fillMaxSize()) {
                if (clipStart > 0f) {
                    Spacer(modifier = Modifier.weight(clipStart))
                }

                Box(
                    modifier = Modifier
                        .weight((clipEnd - clipStart).coerceAtLeast(1f))
                        .height(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            accentColor.copy(
                                alpha = when {
                                    !layer.isVisible -> 0.16f
                                    isSelected -> 0.86f
                                    else -> 0.42f
                                }
                            )
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                Color.White.copy(alpha = if (layer.isVisible) 0.72f else 0.22f)
                            } else {
                                accentColor.copy(alpha = if (layer.isVisible) 0.5f else 0.18f)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(layer.id, trackWidthPx) {
                                var dragSeconds = 0f
                                var dragStartLayer: AtmosphereLayer? = null

                                detectDragGestures(
                                    onDragStart = {
                                        dragSeconds = 0f
                                        dragStartLayer = latestLayerState.value
                                        onClick()
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val baseLayer = dragStartLayer ?: latestLayerState.value
                                        dragSeconds += (dragAmount.x / trackWidthPx) * TIMELINE_DURATION_SECONDS
                                        onClipMove(moveLayerToStart(baseLayer, baseLayer.startTime + dragSeconds))
                                    },
                                    onDragEnd = {
                                        dragStartLayer = null
                                    },
                                    onDragCancel = {
                                        dragStartLayer = null
                                    }
                                )
                            }
                            .clickable { onClick() }
                    )

                    Text(
                        text = clipLabel(layer),
                        color = Color.White.copy(alpha = if (layer.isVisible) 1f else 0.5f),
                        maxLines = 1,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )

                    TimelineTrimHandle(
                        modifier = Modifier.align(Alignment.CenterStart),
                        selected = isSelected,
                        visible = layer.isVisible,
                        onDrag = { dragAmountX ->
                            val baseLayer = latestLayerState.value
                            val seconds = (dragAmountX / trackWidthPx) * TIMELINE_DURATION_SECONDS
                            onClick()
                            onClipMove(trimLayerStartTo(baseLayer, baseLayer.startTime + seconds))
                        }
                    )

                    TimelineTrimHandle(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        selected = isSelected,
                        visible = layer.isVisible,
                        onDrag = { dragAmountX ->
                            val baseLayer = latestLayerState.value
                            val seconds = (dragAmountX / trackWidthPx) * TIMELINE_DURATION_SECONDS
                            onClick()
                            onClipMove(trimLayerEndTo(baseLayer, baseLayer.endTime + seconds))
                        }
                    )
                }

                if (duration - clipEnd > 0f) {
                    Spacer(modifier = Modifier.weight(duration - clipEnd))
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                if (playhead > 0f) {
                    Spacer(modifier = Modifier.weight(playhead))
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(Color.White.copy(alpha = 0.85f))
                )
                if (duration - playhead > 0f) {
                    Spacer(modifier = Modifier.weight(duration - playhead))
                }
            }
        }
    }
}

@Composable
private fun TimelineTrimHandle(
    modifier: Modifier = Modifier,
    selected: Boolean,
    visible: Boolean,
    onDrag: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .width(if (selected) 12.dp else 8.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Color.White.copy(
                    alpha = when {
                        !visible -> 0.08f
                        selected -> 0.5f
                        else -> 0.12f
                    }
                )
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = if (selected && visible) 0.72f else 0.22f))
        )
    }
}

private fun layerTypeLabel(type: AtmosphereLayerType): String {
    return when (type) {
        AtmosphereLayerType.Character -> "Character"
        AtmosphereLayerType.Text -> "Text"
        AtmosphereLayerType.Effect -> "Effect"
        AtmosphereLayerType.Background -> "Background"
        AtmosphereLayerType.Wave -> "Wave"
    }
}

private fun layerTypeShort(type: AtmosphereLayerType): String {
    return when (type) {
        AtmosphereLayerType.Character -> "C"
        AtmosphereLayerType.Text -> "T"
        AtmosphereLayerType.Effect -> "FX"
        AtmosphereLayerType.Background -> "BG"
        AtmosphereLayerType.Wave -> "WV"
    }
}

private fun clipLabel(layer: AtmosphereLayer): String {
    return when (layer.type) {
        AtmosphereLayerType.Character -> "PNG"
        AtmosphereLayerType.Text -> layer.name
        AtmosphereLayerType.Effect -> layer.animationOut
        AtmosphereLayerType.Background -> "BG"
        AtmosphereLayerType.Wave -> "Wave"
    }
}

private fun colorForLayer(type: AtmosphereLayerType): Color {
    return when (type) {
        AtmosphereLayerType.Character -> Color(0xFF8A5CFF)
        AtmosphereLayerType.Text -> Color(0xFFB85CFF)
        AtmosphereLayerType.Effect -> Color(0xFFFF4D8D)
        AtmosphereLayerType.Background -> Color(0xFF4D8DFF)
        AtmosphereLayerType.Wave -> Color(0xFF19D3C5)
    }
}

private fun formatTimelineTime(seconds: Float): String {
    val safeSeconds = seconds
        .coerceIn(0f, TIMELINE_DURATION_SECONDS)
        .roundToInt()
    val minutes = safeSeconds / 60
    val remainingSeconds = safeSeconds % 60

    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}
