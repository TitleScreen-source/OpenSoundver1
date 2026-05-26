package com.opensound.app.models

data class AtmosphereConfig(
    val characterX: Float = 0f,
    val characterY: Float = -32f,
    val characterSize: Float = 105f
)

fun limitAtmosphereConfig(config: AtmosphereConfig): AtmosphereConfig {
    return AtmosphereConfig(
        characterX = config.characterX.coerceIn(-120f, 120f),
        characterY = config.characterY.coerceIn(-90f, 0f),
        characterSize = config.characterSize.coerceIn(70f, 150f)
    )
}