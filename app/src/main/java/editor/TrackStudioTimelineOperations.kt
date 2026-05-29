package com.opensound.app.editor

import com.opensound.app.editor.TrackStudioLayerIds.CHARACTER_MAIN_LAYER_ID
import com.opensound.app.editor.TrackStudioLayerIds.TEXT_MAIN_LAYER_ID
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType

const val TIMELINE_DURATION_SECONDS = 100f

private val PROTECTED_LAYER_IDS = setOf(
    TrackStudioLayerIds.CHARACTER_MAIN_LAYER_ID,
    TrackStudioLayerIds.TEXT_MAIN_LAYER_ID,
    TrackStudioLayerIds.EFFECT_MAIN_LAYER_ID,
    TrackStudioLayerIds.BACKGROUND_MAIN_LAYER_ID,
    TrackStudioLayerIds.WAVE_MAIN_LAYER_ID
)

fun timelineLayersFor(config: AtmosphereConfig): List<AtmosphereLayer> {
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

fun syncPrimaryLayers(config: AtmosphereConfig): AtmosphereConfig {
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

fun updateTimelineLayer(
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

fun moveLayerTime(
    layer: AtmosphereLayer,
    seconds: Float
): AtmosphereLayer {
    return moveLayerToStart(layer, layer.startTime + seconds)
}

fun moveLayerToStart(
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

fun trimLayerStart(
    layer: AtmosphereLayer,
    seconds: Float
): AtmosphereLayer {
    return trimLayerStartTo(layer, layer.startTime + seconds)
}

fun trimLayerStartTo(
    layer: AtmosphereLayer,
    startTime: Float
): AtmosphereLayer {
    val latestStart = (layer.endTime - 2f).coerceAtLeast(0f)
    val nextStart = startTime.coerceIn(0f, latestStart)

    return layer.copy(startTime = nextStart)
}

fun trimLayerEnd(
    layer: AtmosphereLayer,
    seconds: Float
): AtmosphereLayer {
    return trimLayerEndTo(layer, layer.endTime + seconds)
}

fun trimLayerEndTo(
    layer: AtmosphereLayer,
    endTime: Float
): AtmosphereLayer {
    val earliestEnd = (layer.startTime + 2f).coerceAtMost(TIMELINE_DURATION_SECONDS)
    val nextEnd = endTime.coerceIn(earliestEnd, TIMELINE_DURATION_SECONDS)

    return layer.copy(endTime = nextEnd)
}

fun snapLayerToPlayhead(
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

fun layerDuration(layer: AtmosphereLayer): Float {
    return (layer.endTime - layer.startTime).coerceIn(2f, TIMELINE_DURATION_SECONDS)
}

fun addTimelineLayer(
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

fun deleteSelectedLayer(
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

fun toggleLayerVisibility(
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

fun isProtectedLayer(layerId: String): Boolean {
    return layerId in PROTECTED_LAYER_IDS
}

fun duplicateSelectedLayer(
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
