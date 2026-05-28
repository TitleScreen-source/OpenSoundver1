package com.opensound.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType
import com.opensound.app.models.Track
import com.opensound.app.models.atmospherePresets
import com.opensound.app.models.limitAtmosphereConfig
import com.opensound.app.player.AtmosphereMiniPlayerContent
import kotlin.math.roundToInt

private const val CHARACTER_MAIN_LAYER_ID = "character-main"
private const val TEXT_MAIN_LAYER_ID = "text-main"
private const val EFFECT_MAIN_LAYER_ID = "effect-glow"
private const val BACKGROUND_MAIN_LAYER_ID = "background-main"
private const val WAVE_MAIN_LAYER_ID = "wave-main"
private const val TIMELINE_DURATION_SECONDS = 100f
private val PROTECTED_LAYER_IDS = setOf(
    CHARACTER_MAIN_LAYER_ID,
    TEXT_MAIN_LAYER_ID,
    EFFECT_MAIN_LAYER_ID,
    BACKGROUND_MAIN_LAYER_ID,
    WAVE_MAIN_LAYER_ID
)

@Composable
fun TrackStudioScreen(
    track: Track,
    initialConfig: AtmosphereConfig,
    onSave: (AtmosphereConfig) -> Unit,
    onClose: () -> Unit
) {
    var draftConfig by remember(track.title) {
        mutableStateOf(initialConfig)
    }
    var selectedSection by remember {
        mutableStateOf("Scene")
    }
    var previewTimeSeconds by remember {
        mutableStateOf(24f)
    }
    var selectedLayerId by remember {
        mutableStateOf(TEXT_MAIN_LAYER_ID)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08070D))
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        StudioHeader(track = track, onClose = onClose)

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Live mini-player preview",
            color = Color(0xFFC8BED8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        PreviewCard(
            track = track,
            draftConfig = draftConfig,
            previewTimeSeconds = previewTimeSeconds,
            activeDragLayer = selectedSection,
            onDragCharacter = { dx, dy ->
                draftConfig = limitAtmosphereConfig(
                    moveSelectedLayer(
                        config = draftConfig,
                        selectedLayerId = selectedLayerId,
                        type = AtmosphereLayerType.Character,
                        dx = dx,
                        dy = dy
                    )
                )
            },
            onDragText = { dx, dy ->
                draftConfig = limitAtmosphereConfig(
                    moveSelectedLayer(
                        config = draftConfig,
                        selectedLayerId = selectedLayerId,
                        type = AtmosphereLayerType.Text,
                        dx = dx,
                        dy = dy
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        TimelinePanel(
            layers = timelineLayersFor(draftConfig),
            previewTimeSeconds = previewTimeSeconds,
            onPreviewTimeChange = { previewTimeSeconds = it },
            selectedLayerId = selectedLayerId,
            onLayerTimelineEdit = { updatedLayer ->
                draftConfig = limitAtmosphereConfig(
                    updateTimelineLayer(
                        config = draftConfig,
                        updatedLayer = updatedLayer
                    )
                )
            },
            onAddLayer = { type ->
                addTimelineLayer(
                    config = draftConfig,
                    type = type,
                    playheadSeconds = previewTimeSeconds
                ).let { result ->
                    draftConfig = result.first
                    selectedLayerId = result.second
                    selectedSection = sectionForType(type)
                }
            },
            onDuplicateLayer = {
                duplicateSelectedLayer(
                    config = draftConfig,
                    selectedLayerId = selectedLayerId
                )?.let { result ->
                    draftConfig = result.first
                    selectedLayerId = result.second
                    selectedSection = "Timing"
                }
            },
            onDeleteLayer = {
                deleteSelectedLayer(
                    config = draftConfig,
                    selectedLayerId = selectedLayerId
                )?.let { result ->
                    draftConfig = result.first
                    selectedLayerId = result.second
                    selectedSection = timelineLayersFor(result.first)
                        .firstOrNull { it.id == result.second }
                        ?.let { sectionForLayer(it) }
                        ?: "Timing"
                }
            },
            onToggleLayerVisibility = { layer ->
                draftConfig = syncPrimaryLayers(
                    toggleLayerVisibility(
                        config = draftConfig,
                        layerId = layer.id
                    )
                )
            },
            onLayerSelected = { layer ->
                selectedLayerId = layer.id
                selectedSection = sectionForLayer(layer)
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        StudioSections(
            selectedSection = selectedSection,
            onSectionSelected = { selectedSection = it }
        )

        Spacer(modifier = Modifier.height(18.dp))

        when (selectedSection) {
            "Scene" -> SceneSection(
                draftConfig = draftConfig,
                onConfigChange = { draftConfig = limitAtmosphereConfig(syncPrimaryLayers(it)) }
            )

            "Character" -> CharacterSection(
                draftConfig = draftConfig,
                selectedLayerId = selectedLayerId,
                onConfigChange = { draftConfig = limitAtmosphereConfig(syncPrimaryLayers(it)) }
            )

            "Text" -> TextSection(
                draftConfig = draftConfig,
                selectedLayerId = selectedLayerId,
                onConfigChange = { draftConfig = limitAtmosphereConfig(syncPrimaryLayers(it)) }
            )

            "Timing" -> TimingSection(
                draftConfig = draftConfig,
                selectedLayer = timelineLayersFor(draftConfig).firstOrNull { it.id == selectedLayerId },
                previewTimeSeconds = previewTimeSeconds,
                onConfigChange = { draftConfig = limitAtmosphereConfig(it) },
                onLayerChange = { updatedLayer ->
                    draftConfig = limitAtmosphereConfig(
                        updateTimelineLayer(
                            config = draftConfig,
                            updatedLayer = updatedLayer
                        )
                    )
                }
            )

            "Assets" -> AssetsSection(
                draftConfig = draftConfig
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = { draftConfig = AtmosphereConfig() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset", color = Color.White)
            }

            Button(
                onClick = { onSave(limitAtmosphereConfig(draftConfig)) },
                modifier = Modifier.weight(1.4f)
            ) {
                Text("Save atmosphere")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StudioHeader(
    track: Track,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Track Studio",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = track.title,
                color = Color(0xFFC8BED8),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        TextButton(onClick = onClose) {
            Text("\u0417\u0430\u043A\u0440\u044B\u0442\u044C", color = Color.White)
        }
    }
}

@Composable
private fun PreviewCard(
    track: Track,
    draftConfig: AtmosphereConfig,
    previewTimeSeconds: Float,
    activeDragLayer: String,
    onDragCharacter: (Float, Float) -> Unit,
    onDragText: (Float, Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11101A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            AtmosphereMiniPlayerContent(
                track = track,
            isPlaying = true,
            atmosphereConfig = draftConfig,
            onPlayPauseClick = {},
            onOpenFullPlayer = {},
            currentTimeSeconds = previewTimeSeconds,
            editorDragMode = activeDragLayer,
            onCharacterDrag = onDragCharacter,
            onTextDrag = onDragText,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun TimelinePanel(
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
                text = "Clip controls",
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
                        label = "Start -1",
                        modifier = Modifier.width(88.dp),
                        onClick = { onLayerTimelineEdit(trimLayerStart(layer, -1f)) }
                    )
                }
                item {
                    TimelineActionButton(
                        label = "Start +1",
                        modifier = Modifier.width(88.dp),
                        onClick = { onLayerTimelineEdit(trimLayerStart(layer, 1f)) }
                    )
                }
                item {
                    TimelineActionButton(
                        label = "End -1",
                        modifier = Modifier.width(78.dp),
                        onClick = { onLayerTimelineEdit(trimLayerEnd(layer, -1f)) }
                    )
                }
                item {
                    TimelineActionButton(
                        label = "End +1",
                        modifier = Modifier.width(78.dp),
                        onClick = { onLayerTimelineEdit(trimLayerEnd(layer, 1f)) }
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
        Spacer(modifier = Modifier.width(128.dp))

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

    Row(
        modifier = Modifier.fillMaxWidth(),
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
                                    isSelected -> 0.72f
                                    else -> 0.42f
                                }
                            )
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                Color.White.copy(alpha = if (layer.isVisible) 0.55f else 0.22f)
                            } else {
                                accentColor.copy(alpha = if (layer.isVisible) 0.5f else 0.18f)
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
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
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = clipLabel(layer),
                        color = Color.White.copy(alpha = if (layer.isVisible) 1f else 0.5f),
                        maxLines = 1,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
private fun StudioSections(
    selectedSection: String,
    onSectionSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(listOf("Scene", "Character", "Text", "Timing", "Assets")) { section ->
            SectionChip(
                name = section,
                selected = selectedSection == section,
                onClick = { onSectionSelected(section) }
            )
        }
    }
}

private fun timelineLayersFor(config: AtmosphereConfig): List<AtmosphereLayer> {
    return config.layers.map { layer ->
        when (layer.type) {
            AtmosphereLayerType.Text -> {
                val layerText = if (layer.id == TEXT_MAIN_LAYER_ID) {
                    config.overlayText
                } else {
                    layer.text
                }

                layer.copy(
                    name = layerText.ifBlank { "Text cue" },
                    text = layerText,
                    startTime = if (layer.id == TEXT_MAIN_LAYER_ID) config.overlayTextStart else layer.startTime,
                    endTime = if (layer.id == TEXT_MAIN_LAYER_ID) config.overlayTextEnd else layer.endTime,
                    x = if (layer.id == TEXT_MAIN_LAYER_ID) config.overlayTextX else layer.x,
                    y = if (layer.id == TEXT_MAIN_LAYER_ID) config.overlayTextY else layer.y,
                    animationIn = if (layer.id == TEXT_MAIN_LAYER_ID) config.overlayTextAnimation else layer.animationIn
                )
            }
            AtmosphereLayerType.Character -> layer.copy(
                x = if (layer.id == CHARACTER_MAIN_LAYER_ID) config.characterX else layer.x,
                y = if (layer.id == CHARACTER_MAIN_LAYER_ID) config.characterY else layer.y,
                scale = if (layer.id == CHARACTER_MAIN_LAYER_ID) config.characterSize / 100f else layer.scale
            )
            else -> layer
        }
    }
}

private fun sectionForLayer(layer: AtmosphereLayer): String {
    return sectionForType(layer.type)
}

private fun sectionForType(type: AtmosphereLayerType): String {
    return when (type) {
        AtmosphereLayerType.Character -> "Character"
        AtmosphereLayerType.Text -> "Text"
        AtmosphereLayerType.Effect -> "Scene"
        AtmosphereLayerType.Background -> "Assets"
        AtmosphereLayerType.Wave -> "Timing"
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

private fun syncConfigWithLayer(
    config: AtmosphereConfig,
    layer: AtmosphereLayer
): AtmosphereConfig {
    return when {
        layer.id == TEXT_MAIN_LAYER_ID && layer.type == AtmosphereLayerType.Text -> config.copy(
            overlayTextStart = layer.startTime,
            overlayTextEnd = layer.endTime,
            overlayTextAnimation = layer.animationIn,
            overlayTextX = layer.x,
            overlayTextY = layer.y,
            overlayText = layer.text
        )
        layer.id == CHARACTER_MAIN_LAYER_ID && layer.type == AtmosphereLayerType.Character -> config.copy(
            characterX = layer.x,
            characterY = layer.y,
            characterSize = (layer.scale * 100f).coerceIn(70f, 150f)
        )
        else -> config
    }
}

private fun syncPrimaryLayers(config: AtmosphereConfig): AtmosphereConfig {
    return config.copy(
        layers = config.layers.map { layer ->
            when (layer.id) {
                CHARACTER_MAIN_LAYER_ID -> layer.copy(
                    x = config.characterX,
                    y = config.characterY,
                    scale = config.characterSize / 100f,
                    accentColor = config.accentColor
                )
                TEXT_MAIN_LAYER_ID -> layer.copy(
                    x = config.overlayTextX,
                    y = config.overlayTextY,
                    text = config.overlayText,
                    startTime = config.overlayTextStart,
                    endTime = config.overlayTextEnd,
                    animationIn = config.overlayTextAnimation,
                    accentColor = config.accentColor
                )
                else -> layer
            }
        }
    )
}

private fun updateTimelineLayer(
    config: AtmosphereConfig,
    updatedLayer: AtmosphereLayer
): AtmosphereConfig {
    return syncPrimaryLayers(
        syncConfigWithLayer(
            config = config.copy(
                layers = config.layers.map { layer ->
                    if (layer.id == updatedLayer.id) updatedLayer else layer
                }
            ),
            layer = updatedLayer
        )
    )
}

private fun selectedLayerFor(
    config: AtmosphereConfig,
    selectedLayerId: String,
    type: AtmosphereLayerType
): AtmosphereLayer? {
    val layers = timelineLayersFor(config)
    return layers.firstOrNull { it.id == selectedLayerId && it.type == type }
        ?: layers.firstOrNull { it.id == primaryLayerIdFor(type) }
}

private fun primaryLayerIdFor(type: AtmosphereLayerType): String? {
    return when (type) {
        AtmosphereLayerType.Character -> CHARACTER_MAIN_LAYER_ID
        AtmosphereLayerType.Text -> TEXT_MAIN_LAYER_ID
        else -> null
    }
}

private fun moveSelectedLayer(
    config: AtmosphereConfig,
    selectedLayerId: String,
    type: AtmosphereLayerType,
    dx: Float,
    dy: Float
): AtmosphereConfig {
    val layer = selectedLayerFor(config, selectedLayerId, type) ?: return config
    val (minX, maxX) = xBoundsFor(type)
    val (minY, maxY) = yBoundsFor(type)
    val nextX = (layer.x + dx).coerceIn(minX, maxX)
    val nextY = (layer.y + dy).coerceIn(minY, maxY)
    val updatedConfig = when (layer.id) {
        CHARACTER_MAIN_LAYER_ID -> config.copy(
            characterX = nextX,
            characterY = nextY
        )
        TEXT_MAIN_LAYER_ID -> config.copy(
            overlayTextX = nextX,
            overlayTextY = nextY
        )
        else -> config.copy(
            layers = config.layers.map { currentLayer ->
                if (currentLayer.id == layer.id) {
                    currentLayer.copy(x = nextX, y = nextY)
                } else {
                    currentLayer
                }
            }
        )
    }

    return syncPrimaryLayers(updatedConfig)
}

private fun updateSelectedCharacterSize(
    config: AtmosphereConfig,
    selectedLayerId: String,
    size: Float
): AtmosphereConfig {
    val layer = selectedLayerFor(config, selectedLayerId, AtmosphereLayerType.Character) ?: return config
    val safeSize = size.coerceIn(70f, 150f)
    val updatedConfig = when (layer.id) {
        CHARACTER_MAIN_LAYER_ID -> config.copy(characterSize = safeSize)
        else -> config.copy(
            layers = config.layers.map { currentLayer ->
                if (currentLayer.id == layer.id) {
                    currentLayer.copy(scale = safeSize / 100f)
                } else {
                    currentLayer
                }
            }
        )
    }

    return syncPrimaryLayers(updatedConfig)
}

private fun updateSelectedText(
    config: AtmosphereConfig,
    selectedLayerId: String,
    text: String
): AtmosphereConfig {
    val layer = selectedLayerFor(config, selectedLayerId, AtmosphereLayerType.Text) ?: return config
    val safeText = text.take(28)
    val updatedConfig = when (layer.id) {
        TEXT_MAIN_LAYER_ID -> config.copy(overlayText = safeText)
        else -> config.copy(
            layers = config.layers.map { currentLayer ->
                if (currentLayer.id == layer.id) {
                    currentLayer.copy(
                        name = safeText.ifBlank { "Text cue" },
                        text = safeText
                    )
                } else {
                    currentLayer
                }
            }
        )
    }

    return syncPrimaryLayers(updatedConfig)
}

private fun updateSelectedTextAnimation(
    config: AtmosphereConfig,
    selectedLayerId: String,
    animation: String
): AtmosphereConfig {
    val layer = selectedLayerFor(config, selectedLayerId, AtmosphereLayerType.Text) ?: return config
    val updatedConfig = when (layer.id) {
        TEXT_MAIN_LAYER_ID -> config.copy(overlayTextAnimation = animation)
        else -> config.copy(
            layers = config.layers.map { currentLayer ->
                if (currentLayer.id == layer.id) {
                    currentLayer.copy(animationIn = animation)
                } else {
                    currentLayer
                }
            }
        )
    }

    return syncPrimaryLayers(updatedConfig)
}

private fun xBoundsFor(type: AtmosphereLayerType): Pair<Float, Float> {
    return when (type) {
        AtmosphereLayerType.Text -> -240f to 240f
        AtmosphereLayerType.Character -> -260f to 260f
        else -> -260f to 260f
    }
}

private fun yBoundsFor(type: AtmosphereLayerType): Pair<Float, Float> {
    return when (type) {
        AtmosphereLayerType.Text -> -55f to 105f
        AtmosphereLayerType.Character -> -155f to 70f
        else -> -155f to 105f
    }
}

private fun moveLayerTime(
    layer: AtmosphereLayer,
    seconds: Float
): AtmosphereLayer {
    return moveLayerToStart(layer, layer.startTime + seconds)
}

private fun moveLayerToStart(
    layer: AtmosphereLayer,
    startTime: Float
): AtmosphereLayer {
    val duration = layerDuration(layer)
    val nextStart = startTime.coerceIn(0f, TIMELINE_DURATION_SECONDS - duration)

    return layer.copy(
        startTime = nextStart,
        endTime = nextStart + duration
    )
}

private fun trimLayerStart(
    layer: AtmosphereLayer,
    seconds: Float
): AtmosphereLayer {
    val latestStart = (layer.endTime - 2f).coerceAtLeast(0f)
    val nextStart = (layer.startTime + seconds).coerceIn(0f, latestStart)

    return layer.copy(startTime = nextStart)
}

private fun trimLayerEnd(
    layer: AtmosphereLayer,
    seconds: Float
): AtmosphereLayer {
    val earliestEnd = (layer.startTime + 2f).coerceAtMost(TIMELINE_DURATION_SECONDS)
    val nextEnd = (layer.endTime + seconds).coerceIn(earliestEnd, TIMELINE_DURATION_SECONDS)

    return layer.copy(endTime = nextEnd)
}

private fun snapLayerToPlayhead(
    layer: AtmosphereLayer,
    playheadSeconds: Float
): AtmosphereLayer {
    val duration = layerDuration(layer)
    val nextStart = playheadSeconds.coerceIn(0f, TIMELINE_DURATION_SECONDS - duration)

    return layer.copy(
        startTime = nextStart,
        endTime = nextStart + duration
    )
}

private fun layerDuration(layer: AtmosphereLayer): Float {
    return (layer.endTime - layer.startTime).coerceIn(2f, TIMELINE_DURATION_SECONDS)
}

private fun addTimelineLayer(
    config: AtmosphereConfig,
    type: AtmosphereLayerType,
    playheadSeconds: Float
): Pair<AtmosphereConfig, String> {
    val start = playheadSeconds.coerceIn(0f, TIMELINE_DURATION_SECONDS - 2f)
    val duration = defaultLayerDuration(type)
    val end = (start + duration).coerceIn(start + 2f, TIMELINE_DURATION_SECONDS)
    val idPrefix = layerIdPrefix(type)
    val newId = nextLayerId(config, idPrefix)
    val layerNumber = config.layers.count { it.type == type } + 1
    val layer = when (type) {
        AtmosphereLayerType.Character -> AtmosphereLayer(
            id = newId,
            type = type,
            name = "Character $layerNumber",
            startTime = start,
            endTime = end,
            assetRef = "test-character.png",
            x = config.characterX,
            y = config.characterY,
            scale = config.characterSize / 100f,
            accentColor = config.accentColor
        )
        AtmosphereLayerType.Text -> AtmosphereLayer(
            id = newId,
            type = type,
            name = "Text cue $layerNumber",
            startTime = start,
            endTime = end,
            y = -10f,
            text = "TEXT CUE",
            accentColor = config.accentColor
        )
        AtmosphereLayerType.Effect -> AtmosphereLayer(
            id = newId,
            type = type,
            name = "Effect $layerNumber",
            startTime = start,
            endTime = end,
            accentColor = config.accentColor,
            animationIn = "Fade",
            animationOut = "Pulse"
        )
        AtmosphereLayerType.Background -> AtmosphereLayer(
            id = newId,
            type = type,
            name = "Background $layerNumber",
            startTime = start,
            endTime = end,
            assetRef = "test-cover.jpg",
            accentColor = config.accentColor
        )
        AtmosphereLayerType.Wave -> AtmosphereLayer(
            id = newId,
            type = type,
            name = "Wave $layerNumber",
            startTime = start,
            endTime = end,
            assetRef = "test-audio.mp3",
            accentColor = config.accentColor
        )
    }

    return syncPrimaryLayers(
        config.copy(layers = config.layers + layer)
    ) to newId
}

private fun deleteSelectedLayer(
    config: AtmosphereConfig,
    selectedLayerId: String
): Pair<AtmosphereConfig, String>? {
    if (isProtectedLayer(selectedLayerId)) return null

    val layerIndex = config.layers.indexOfFirst { it.id == selectedLayerId }
    if (layerIndex == -1) return null

    val nextLayers = config.layers.filterNot { it.id == selectedLayerId }
    val nextSelectedId = nextLayers
        .getOrNull(layerIndex.coerceAtMost(nextLayers.lastIndex))
        ?.id
        ?: TEXT_MAIN_LAYER_ID

    return syncPrimaryLayers(
        config.copy(layers = nextLayers)
    ) to nextSelectedId
}

private fun toggleLayerVisibility(
    config: AtmosphereConfig,
    layerId: String
): AtmosphereConfig {
    return config.copy(
        layers = config.layers.map { layer ->
            if (layer.id == layerId) {
                layer.copy(isVisible = !layer.isVisible)
            } else {
                layer
            }
        }
    )
}

private fun isProtectedLayer(layerId: String): Boolean {
    return layerId in PROTECTED_LAYER_IDS
}

private fun nextLayerId(
    config: AtmosphereConfig,
    prefix: String
): String {
    var nextIndex = config.layers.count { it.id.startsWith(prefix) } + 1
    var nextId = "$prefix-$nextIndex"

    while (config.layers.any { it.id == nextId }) {
        nextIndex += 1
        nextId = "$prefix-$nextIndex"
    }

    return nextId
}

private fun layerIdPrefix(type: AtmosphereLayerType): String {
    return when (type) {
        AtmosphereLayerType.Character -> "character"
        AtmosphereLayerType.Text -> "text"
        AtmosphereLayerType.Effect -> "effect"
        AtmosphereLayerType.Background -> "background"
        AtmosphereLayerType.Wave -> "wave"
    }
}

private fun defaultLayerDuration(type: AtmosphereLayerType): Float {
    return when (type) {
        AtmosphereLayerType.Character -> 16f
        AtmosphereLayerType.Text -> 12f
        AtmosphereLayerType.Effect -> 10f
        AtmosphereLayerType.Background -> 24f
        AtmosphereLayerType.Wave -> 24f
    }
}

private fun duplicateSelectedLayer(
    config: AtmosphereConfig,
    selectedLayerId: String
): Pair<AtmosphereConfig, String>? {
    val selectedLayer = timelineLayersFor(config).firstOrNull { it.id == selectedLayerId } ?: return null
    val copyIndex = config.layers.count { it.id.startsWith("${selectedLayer.id}-copy") } + 1
    val duration = (selectedLayer.endTime - selectedLayer.startTime).coerceAtLeast(2f)
    val newStart = (selectedLayer.startTime + 4f).coerceIn(0f, TIMELINE_DURATION_SECONDS - 2f)
    val newEnd = (newStart + duration).coerceIn(newStart + 2f, TIMELINE_DURATION_SECONDS)
    val newId = "${selectedLayer.id}-copy-$copyIndex"
    val duplicatedLayer = selectedLayer.copy(
        id = newId,
        name = "${selectedLayer.name} Copy",
        startTime = newStart,
        endTime = newEnd.coerceAtLeast(newStart + 2f)
    )

    return config.copy(
        layers = config.layers + duplicatedLayer
    ) to newId
}

@Composable
private fun SceneSection(
    draftConfig: AtmosphereConfig,
    onConfigChange: (AtmosphereConfig) -> Unit
) {
    StudioPanel(title = "Scene style") {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(atmospherePresets) { preset ->
                PresetChip(
                    name = preset.presetName,
                    color = Color(preset.accentColor),
                    selected = draftConfig.presetName == preset.presetName,
                    onClick = {
                        onConfigChange(
                            preset.copy(
                                characterX = draftConfig.characterX,
                                characterY = draftConfig.characterY,
                                characterSize = draftConfig.characterSize,
                                overlayText = draftConfig.overlayText,
                                overlayTextX = draftConfig.overlayTextX,
                                overlayTextY = draftConfig.overlayTextY,
                                overlayTextStart = draftConfig.overlayTextStart,
                                overlayTextEnd = draftConfig.overlayTextEnd,
                                overlayTextAnimation = draftConfig.overlayTextAnimation,
                                layers = draftConfig.layers
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text("Accent color", color = Color.White, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(0xFF9B5CFF, 0xFF00D4FF, 0xFFFF4D8D, 0xFFB7FF5C).forEach { color ->
                ColorSwatch(
                    color = Color(color),
                    selected = draftConfig.accentColor == color,
                    onClick = {
                        onConfigChange(
                            draftConfig.copy(
                                accentColor = color,
                                presetName = "Custom"
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        StudioSlider(
            title = "Glow intensity",
            value = draftConfig.glowIntensity,
            valueRange = 0f..1f,
            onValueChange = {
                onConfigChange(draftConfig.copy(glowIntensity = it))
            }
        )

        StudioSlider(
            title = "Panel opacity",
            value = draftConfig.panelOpacity,
            valueRange = 0.55f..1f,
            onValueChange = {
                onConfigChange(draftConfig.copy(panelOpacity = it))
            }
        )
    }
}

@Composable
private fun CharacterSection(
    draftConfig: AtmosphereConfig,
    selectedLayerId: String,
    onConfigChange: (AtmosphereConfig) -> Unit
) {
    val layer = selectedLayerFor(draftConfig, selectedLayerId, AtmosphereLayerType.Character)
    val targetLayerId = layer?.id ?: CHARACTER_MAIN_LAYER_ID
    val size = ((layer?.scale ?: (draftConfig.characterSize / 100f)) * 100f).coerceIn(70f, 150f)
    val positionX = layer?.x ?: draftConfig.characterX
    val positionY = layer?.y ?: draftConfig.characterY

    StudioPanel(title = "Character layer") {
        StudioSlider(
            title = "Character size",
            value = size,
            valueRange = 70f..150f,
            onValueChange = {
                onConfigChange(updateSelectedCharacterSize(draftConfig, targetLayerId, it))
            }
        )

        Text(
            text = "Layer: ${layer?.name ?: "Character"}. Drag in preview. Position: x=${positionX.roundToInt()}, y=${positionY.roundToInt()}",
            color = Color(0xFFA9A1B6)
        )
    }
}

@Composable
private fun TextSection(
    draftConfig: AtmosphereConfig,
    selectedLayerId: String,
    onConfigChange: (AtmosphereConfig) -> Unit
) {
    val layer = selectedLayerFor(draftConfig, selectedLayerId, AtmosphereLayerType.Text)
    val targetLayerId = layer?.id ?: TEXT_MAIN_LAYER_ID
    val textValue = layer?.text ?: draftConfig.overlayText
    val animationValue = layer?.animationIn ?: draftConfig.overlayTextAnimation
    val positionX = layer?.x ?: draftConfig.overlayTextX
    val positionY = layer?.y ?: draftConfig.overlayTextY

    StudioPanel(title = "Text cue") {
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                onConfigChange(updateSelectedText(draftConfig, targetLayerId, it))
            },
            label = { Text("Phrase") },
            placeholder = { Text("Example: night calls") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Animation", color = Color.White, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf("Fade", "Rise", "Pulse")) { animation ->
                SectionChip(
                    name = animation,
                    selected = animationValue == animation,
                    onClick = {
                        onConfigChange(updateSelectedTextAnimation(draftConfig, targetLayerId, animation))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Layer: ${layer?.name ?: "Text cue"}. Drag in preview. Position: x=${positionX.roundToInt()}, y=${positionY.roundToInt()}",
            color = Color(0xFFA9A1B6)
        )
    }
}

@Composable
private fun TimingSection(
    draftConfig: AtmosphereConfig,
    selectedLayer: AtmosphereLayer?,
    previewTimeSeconds: Float,
    onConfigChange: (AtmosphereConfig) -> Unit,
    onLayerChange: (AtmosphereLayer) -> Unit
) {
    val layer = selectedLayer ?: timelineLayersFor(draftConfig).first()

    StudioPanel(title = "Selected clip") {
        Text(
            text = "${layer.name} - ${layer.startTime.roundToInt()}s to ${layer.endTime.roundToInt()}s",
            color = Color(0xFFC8BED8)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StudioSlider(
            title = "Start time",
            value = layer.startTime,
            valueRange = 0f..100f,
            onValueChange = {
                onLayerChange(
                    layer.copy(
                        startTime = it,
                        endTime = layer.endTime.coerceAtLeast(it + 2f)
                    )
                )
            }
        )

        StudioSlider(
            title = "End time",
            value = layer.endTime,
            valueRange = 0f..100f,
            onValueChange = {
                onLayerChange(
                    layer.copy(
                        endTime = it.coerceAtLeast(layer.startTime + 2f)
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Animation in", color = Color.White, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf("Fade", "Rise", "Pulse")) { animation ->
                SectionChip(
                    name = animation,
                    selected = layer.animationIn == animation,
                    onClick = {
                        onLayerChange(layer.copy(animationIn = animation))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Animation out", color = Color.White, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf("Fade", "Glitch", "Cut")) { animation ->
                SectionChip(
                    name = animation,
                    selected = layer.animationOut == animation,
                    onClick = {
                        onLayerChange(layer.copy(animationOut = animation))
                    }
                )
            }
        }
    }
}

@Composable
private fun AssetsSection(
    draftConfig: AtmosphereConfig
) {
    StudioPanel(title = "Source assets") {
        AssetPlaceholder(
            title = "Character PNG",
            description = "Current build uses a test cutout. Upload flow will replace this layer per track."
        )

        Spacer(modifier = Modifier.height(10.dp))

        AssetPlaceholder(
            title = "Background / GIF",
            description = "Prepared as a future scene layer for animated covers and backgrounds."
        )

        Spacer(modifier = Modifier.height(10.dp))

        AssetPlaceholder(
            title = "Audio track",
            description = "Timing controls are ready to connect to real playback position later."
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Active preset: ${draftConfig.presetName}",
            color = Color(0xFFA9A1B6)
        )
    }
}

@Composable
private fun AssetPlaceholder(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(description, color = Color(0xFFA9A1B6))
    }
}

@Composable
private fun StudioPanel(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        content()
    }
}

@Composable
private fun SectionChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = Color(0xFF9B5CFF)

    Text(
        text = name,
        color = Color.White,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) accentColor.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = if (selected) accentColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun PresetChip(
    name: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) color.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = if (selected) color else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(name, color = Color.White)
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.22f),
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}

@Composable
private fun StudioSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White)
            Text(value.roundToInt().toString(), color = Color(0xFFA9A1B6))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
