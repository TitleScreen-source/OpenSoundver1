package com.opensound.app.models

enum class AtmosphereLayerType {
    Character,
    Text,
    Effect,
    Background,
    Wave
}

data class AtmosphereLayer(
    val id: String,
    val type: AtmosphereLayerType,
    val name: String,
    val startTime: Float,
    val endTime: Float,
    val assetRef: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val text: String = "",
    val accentColor: Long = 0xFF9B5CFF,
    val animationIn: String = "Fade",
    val animationOut: String = "Fade",
    val isVisible: Boolean = true
)

data class AtmosphereScene(
    val trackTitle: String,
    val durationSeconds: Float = 100f,
    val layers: List<AtmosphereLayer> = defaultAtmosphereLayers()
)

fun defaultAtmosphereLayers(): List<AtmosphereLayer> {
    return listOf(
        AtmosphereLayer(
            id = "character-main",
            type = AtmosphereLayerType.Character,
            name = "Character",
            startTime = 0f,
            endTime = 100f,
            assetRef = "test-character.png",
            x = 0f,
            y = -32f,
            scale = 1.05f
        ),
        AtmosphereLayer(
            id = "text-main",
            type = AtmosphereLayerType.Text,
            name = "Text cue",
            startTime = 24f,
            endTime = 42f,
            y = -6f,
            text = ""
        ),
        AtmosphereLayer(
            id = "effect-glow",
            type = AtmosphereLayerType.Effect,
            name = "Glow",
            startTime = 10f,
            endTime = 52f,
            animationIn = "Fade",
            animationOut = "Pulse"
        ),
        AtmosphereLayer(
            id = "background-main",
            type = AtmosphereLayerType.Background,
            name = "Background",
            startTime = 0f,
            endTime = 100f,
            assetRef = "test-cover.jpg"
        ),
        AtmosphereLayer(
            id = "wave-main",
            type = AtmosphereLayerType.Wave,
            name = "Wave",
            startTime = 0f,
            endTime = 100f,
            assetRef = "test-audio.mp3"
        )
    )
}
