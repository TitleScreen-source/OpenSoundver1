package com.opensound.app.models

data class AtmosphereConfig(
    val characterX: Float = 0f,
    val characterY: Float = -32f,
    val characterSize: Float = 105f,
    val accentColor: Long = 0xFF9B5CFF,
    val glowIntensity: Float = 0.75f,
    val panelOpacity: Float = 0.92f,
    val presetName: String = "Night",
    val overlayText: String = "",
    val overlayTextX: Float = 0f,
    val overlayTextY: Float = -6f,
    val overlayTextStart: Float = 24f,
    val overlayTextEnd: Float = 42f,
    val overlayTextAnimation: String = "Fade"
)

fun limitAtmosphereConfig(config: AtmosphereConfig): AtmosphereConfig {
    return config.copy(
        characterX = config.characterX.coerceIn(-260f, 260f),
        characterY = config.characterY.coerceIn(-155f, 70f),
        characterSize = config.characterSize.coerceIn(70f, 150f),
        glowIntensity = config.glowIntensity.coerceIn(0f, 1f),
        panelOpacity = config.panelOpacity.coerceIn(0.55f, 1f),
        overlayTextX = config.overlayTextX.coerceIn(-240f, 240f),
        overlayTextY = config.overlayTextY.coerceIn(-55f, 105f),
        overlayTextStart = config.overlayTextStart.coerceIn(0f, 100f),
        overlayTextEnd = config.overlayTextEnd.coerceIn(0f, 100f)
    )
}

val atmospherePresets = listOf(
    AtmosphereConfig(
        characterX = 0f,
        characterY = -32f,
        characterSize = 105f,
        accentColor = 0xFF9B5CFF,
        glowIntensity = 0.78f,
        panelOpacity = 0.92f,
        presetName = "Night"
    ),
    AtmosphereConfig(
        characterX = 16f,
        characterY = -42f,
        characterSize = 112f,
        accentColor = 0xFF00D4FF,
        glowIntensity = 0.86f,
        panelOpacity = 0.88f,
        presetName = "Neon"
    ),
    AtmosphereConfig(
        characterX = -10f,
        characterY = -28f,
        characterSize = 98f,
        accentColor = 0xFFFF4D8D,
        glowIntensity = 0.62f,
        panelOpacity = 0.86f,
        presetName = "Dream"
    ),
    AtmosphereConfig(
        characterX = 0f,
        characterY = -20f,
        characterSize = 92f,
        accentColor = 0xFFB7FF5C,
        glowIntensity = 0.42f,
        panelOpacity = 0.78f,
        presetName = "Soft"
    )
)
