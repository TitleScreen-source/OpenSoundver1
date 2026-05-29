package com.opensound.app.editor

import com.opensound.app.editor.TrackStudioLayerIds.CHARACTER_MAIN_LAYER_ID
import com.opensound.app.editor.TrackStudioLayerIds.TEXT_MAIN_LAYER_ID
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType

fun selectedLayerFor(
    config: AtmosphereConfig,
    selectedLayerId: String,
    type: AtmosphereLayerType
): AtmosphereLayer? {
    val layers = timelineLayersFor(config)
    return layers.firstOrNull { layer -> layer.id == selectedLayerId && layer.type == type }
        ?: layers.firstOrNull { layer -> layer.id == primaryLayerIdFor(type) }
}

fun moveSelectedLayer(
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

fun updateSelectedCharacterSize(
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

fun updateSelectedText(
    config: AtmosphereConfig,
    selectedLayerId: String,
    text: String
): AtmosphereConfig {
    val layer = selectedLayerFor(config, selectedLayerId, AtmosphereLayerType.Text) ?: return config
    val safeText = text.take(MAX_TEXT_CUE_LENGTH)
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

fun updateSelectedTextAnimation(
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

private const val MAX_TEXT_CUE_LENGTH = 28

private fun primaryLayerIdFor(type: AtmosphereLayerType): String? {
    return when (type) {
        AtmosphereLayerType.Character -> CHARACTER_MAIN_LAYER_ID
        AtmosphereLayerType.Text -> TEXT_MAIN_LAYER_ID
        else -> null
    }
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
