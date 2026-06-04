package com.opensound.app.screens

import androidx.compose.ui.graphics.Color
import com.opensound.app.editor.TIMELINE_DURATION_SECONDS
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType
import kotlin.math.roundToInt

internal fun layerTypeLabel(type: AtmosphereLayerType): String {
    return when (type) {
        AtmosphereLayerType.Character -> "Character"
        AtmosphereLayerType.Text -> "Text"
        AtmosphereLayerType.Effect -> "Effect"
        AtmosphereLayerType.Background -> "Background"
        AtmosphereLayerType.Wave -> "Wave"
    }
}

internal fun layerTypeShort(type: AtmosphereLayerType): String {
    return when (type) {
        AtmosphereLayerType.Character -> "C"
        AtmosphereLayerType.Text -> "T"
        AtmosphereLayerType.Effect -> "FX"
        AtmosphereLayerType.Background -> "BG"
        AtmosphereLayerType.Wave -> "WV"
    }
}

internal fun clipLabel(layer: AtmosphereLayer): String {
    return when (layer.type) {
        AtmosphereLayerType.Character -> "PNG"
        AtmosphereLayerType.Text -> layer.name
        AtmosphereLayerType.Effect -> layer.animationOut
        AtmosphereLayerType.Background -> "BG"
        AtmosphereLayerType.Wave -> "Wave"
    }
}

internal fun colorForLayer(type: AtmosphereLayerType): Color {
    return when (type) {
        AtmosphereLayerType.Character -> Color(0xFF8A5CFF)
        AtmosphereLayerType.Text -> Color(0xFFB85CFF)
        AtmosphereLayerType.Effect -> Color(0xFFFF4D8D)
        AtmosphereLayerType.Background -> Color(0xFF4D8DFF)
        AtmosphereLayerType.Wave -> Color(0xFF19D3C5)
    }
}

internal fun formatTimelineTime(seconds: Float): String {
    val safeSeconds = seconds
        .coerceIn(0f, TIMELINE_DURATION_SECONDS)
        .roundToInt()
    val minutes = safeSeconds / 60
    val remainingSeconds = safeSeconds % 60

    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}
