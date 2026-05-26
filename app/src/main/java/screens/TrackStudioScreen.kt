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
                previewTimeSeconds = previewTimeSeconds,
                onPreviewTimeChange = { previewTimeSeconds = it },
                onConfigChange = { draftConfig = limitAtmosphereConfig(it) }
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
    previewTimeSeconds: Float,
    onPreviewTimeChange: (Float) -> Unit,
    onConfigChange: (AtmosphereConfig) -> Unit
) {
    StudioPanel(title = "Timeline cue") {
        Text(
            text = "Text appears from ${draftConfig.overlayTextStart.roundToInt()}s to ${draftConfig.overlayTextEnd.roundToInt()}s",
            color = Color(0xFFC8BED8)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StudioSlider(
            title = "Preview time",
            value = previewTimeSeconds,
            valueRange = 0f..100f,
            onValueChange = onPreviewTimeChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        StudioSlider(
            title = "Start time",
            value = draftConfig.overlayTextStart,
            valueRange = 0f..100f,
            onValueChange = {
                onConfigChange(
                    draftConfig.copy(
                        overlayTextStart = it,
                        overlayTextEnd = draftConfig.overlayTextEnd.coerceAtLeast(it + 2f)
                    )
                )
            }
        )

        StudioSlider(
            title = "End time",
            value = draftConfig.overlayTextEnd,
            valueRange = 0f..100f,
            onValueChange = {
                onConfigChange(
                    draftConfig.copy(
                        overlayTextEnd = it.coerceAtLeast(draftConfig.overlayTextStart + 2f)
                    )
                )
            }
        )
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
