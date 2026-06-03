package com.opensound.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensound.app.editor.TrackStudioLayerIds.CHARACTER_MAIN_LAYER_ID
import com.opensound.app.editor.TrackStudioLayerIds.TEXT_MAIN_LAYER_ID
import com.opensound.app.editor.selectedLayerFor
import com.opensound.app.editor.timelineLayersFor
import com.opensound.app.editor.updateSelectedCharacterSize
import com.opensound.app.editor.updateSelectedText
import com.opensound.app.editor.updateSelectedTextAnimation
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType
import com.opensound.app.models.atmospherePresets
import kotlin.math.roundToInt

@Composable
internal fun SceneSection(
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
internal fun CharacterSection(
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
internal fun TextSection(
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
internal fun TimingSection(
    draftConfig: AtmosphereConfig,
    selectedLayer: AtmosphereLayer?,
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
internal fun AssetsSection(
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
