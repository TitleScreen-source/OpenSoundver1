package com.opensound.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensound.app.editor.TIMELINE_DURATION_SECONDS
import com.opensound.app.editor.moveLayerToStart
import com.opensound.app.editor.trimLayerEndTo
import com.opensound.app.editor.trimLayerStartTo
import com.opensound.app.models.AtmosphereLayer

@Composable
internal fun TimelineLayerRow(
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
