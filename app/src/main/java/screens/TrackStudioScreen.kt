package com.opensound.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
        mutableStateOf("text-main")
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
                    draftConfig.copy(
                        characterX = draftConfig.characterX + dx,
                        characterY = draftConfig.characterY + dy
                    )
                )
            },
            onDragText = { dx, dy ->
                draftConfig = limitAtmosphereConfig(
                    draftConfig.copy(
                        overlayTextX = draftConfig.overlayTextX + dx,
                        overlayTextY = draftConfig.overlayTextY + dy
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
            onLayerSelected = { layer ->
                selectedLayerId = layer.id
                selectedSection = when (layer.type) {
                    AtmosphereLayerType.Character -> "Character"
                    AtmosphereLayerType.Text -> "Text"
                    AtmosphereLayerType.Effect -> "Scene"
                    AtmosphereLayerType.Background -> "Assets"
                    AtmosphereLayerType.Wave -> "Timing"
                }
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
                onConfigChange = { draftConfig = limitAtmosphereConfig(it) }
            )

            "Character" -> CharacterSection(
                draftConfig = draftConfig,
                onConfigChange = { draftConfig = limitAtmosphereConfig(it) }
            )

            "Text" -> TextSection(
                draftConfig = draftConfig,
                onConfigChange = { draftConfig = limitAtmosphereConfig(it) }
            )

            "Timing" -> TimingSection(
                draftConfig = draftConfig,
                selectedLayer = timelineLayersFor(draftConfig).firstOrNull { it.id == selectedLayerId },
                previewTimeSeconds = previewTimeSeconds,
                onConfigChange = { draftConfig = limitAtmosphereConfig(it) },
                onLayerChange = { updatedLayer ->
                    draftConfig = limitAtmosphereConfig(
                        syncConfigWithLayer(
                            config = draftConfig.copy(
                                layers = draftConfig.layers.map { layer ->
                                    if (layer.id == updatedLayer.id) updatedLayer else layer
                                }
                            ),
                            layer = updatedLayer
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
    onDuplicateLayer: () -> Unit,
    onLayerSelected: (AtmosphereLayer) -> Unit
) {
    val selectedLayer = layers.firstOrNull { it.id == selectedLayerId }

    StudioPanel(title = "Atmosphere timeline") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimelineActionButton("+ Layer", modifier = Modifier.weight(1f))
            TimelineActionButton(
                label = "Duplicate",
                modifier = Modifier.weight(1f),
                onClick = onDuplicateLayer
            )
            TimelineActionButton("Delete", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Playhead: ${previewTimeSeconds.roundToInt()}s" +
                if (selectedLayer != null) " • Selected: ${selectedLayer.name}" else "",
            color = Color(0xFFA9A1B6)
        )

        Slider(
            value = previewTimeSeconds,
            onValueChange = onPreviewTimeChange,
            valueRange = 0f..100f
        )

        Text(
            text = "Tap a clip to edit that layer.",
            color = Color(0xFFA9A1B6)
        )

        Spacer(modifier = Modifier.height(12.dp))

        layers.forEach { layer ->
            TimelineLayerRow(
                layer = layer,
                previewTimeSeconds = previewTimeSeconds,
                isSelected = selectedLayerId == layer.id,
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
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TimelineLayerRow(
    layer: AtmosphereLayer,
    previewTimeSeconds: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = colorForLayer(layer.type)
    val duration = 100f
    val clipStart = layer.startTime.coerceIn(0f, duration)
    val minClipEnd = (clipStart + 1f).coerceAtMost(duration)
    val clipEnd = layer.endTime.coerceIn(clipStart, duration).coerceAtLeast(minClipEnd)
    val playhead = previewTimeSeconds.coerceIn(0f, duration)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = layer.name,
            color = Color(0xFFC8BED8),
            maxLines = 1,
            modifier = Modifier.width(82.dp)
        )

        Box(
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
            Row(modifier = Modifier.fillMaxSize()) {
                if (clipStart > 0f) {
                    Spacer(modifier = Modifier.weight(clipStart))
                }

                Box(
                    modifier = Modifier
                        .weight((clipEnd - clipStart).coerceAtLeast(1f))
                        .height(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = if (isSelected) 0.72f else 0.42f))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White.copy(alpha = 0.55f) else accentColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = clipLabel(layer),
                        color = Color.White,
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
            AtmosphereLayerType.Text -> layer.copy(
                name = if (config.overlayText.isBlank()) "Text cue" else config.overlayText,
                startTime = config.overlayTextStart,
                endTime = config.overlayTextEnd,
                animationIn = config.overlayTextAnimation
            )
            else -> layer
        }
    }
}

private fun sectionForLayer(layer: AtmosphereLayer): String {
    return when (layer.type) {
        AtmosphereLayerType.Character -> "Character"
        AtmosphereLayerType.Text -> "Text"
        AtmosphereLayerType.Effect -> "Scene"
        AtmosphereLayerType.Background -> "Assets"
        AtmosphereLayerType.Wave -> "Timing"
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

private fun syncConfigWithLayer(
    config: AtmosphereConfig,
    layer: AtmosphereLayer
): AtmosphereConfig {
    return when (layer.type) {
        AtmosphereLayerType.Text -> config.copy(
            overlayTextStart = layer.startTime,
            overlayTextEnd = layer.endTime,
            overlayTextAnimation = layer.animationIn
        )
        else -> config
    }
}

private fun duplicateSelectedLayer(
    config: AtmosphereConfig,
    selectedLayerId: String
): Pair<AtmosphereConfig, String>? {
    val selectedLayer = config.layers.firstOrNull { it.id == selectedLayerId } ?: return null
    val copyIndex = config.layers.count { it.id.startsWith("${selectedLayer.id}-copy") } + 1
    val duration = (selectedLayer.endTime - selectedLayer.startTime).coerceAtLeast(2f)
    val newStart = (selectedLayer.startTime + 4f).coerceIn(0f, 98f)
    val newEnd = (newStart + duration).coerceIn(newStart + 2f, 100f)
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
                                overlayTextAnimation = draftConfig.overlayTextAnimation
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
    onConfigChange: (AtmosphereConfig) -> Unit
) {
    StudioPanel(title = "Character layer") {
        StudioSlider(
            title = "Character size",
            value = draftConfig.characterSize,
            valueRange = 70f..150f,
            onValueChange = {
                onConfigChange(draftConfig.copy(characterSize = it))
            }
        )

        Text(
            text = "Drag the character directly in preview. Position: x=${draftConfig.characterX.roundToInt()}, y=${draftConfig.characterY.roundToInt()}",
            color = Color(0xFFA9A1B6)
        )
    }
}

@Composable
private fun TextSection(
    draftConfig: AtmosphereConfig,
    onConfigChange: (AtmosphereConfig) -> Unit
) {
    StudioPanel(title = "Text cue") {
        OutlinedTextField(
            value = draftConfig.overlayText,
            onValueChange = {
                onConfigChange(draftConfig.copy(overlayText = it.take(28)))
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
                    selected = draftConfig.overlayTextAnimation == animation,
                    onClick = {
                        onConfigChange(draftConfig.copy(overlayTextAnimation = animation))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Drag the phrase directly in preview. Position: x=${draftConfig.overlayTextX.roundToInt()}, y=${draftConfig.overlayTextY.roundToInt()}",
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
            text = "${layer.name} • ${layer.startTime.roundToInt()}s to ${layer.endTime.roundToInt()}s",
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
