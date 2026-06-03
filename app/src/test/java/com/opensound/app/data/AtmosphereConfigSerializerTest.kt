package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AtmosphereConfigSerializerTest {
    @Test
    fun decode_readsEncodedConfigWithLayers() {
        val config = AtmosphereConfig(
            characterX = 12f,
            characterY = -48f,
            characterSize = 132f,
            accentColor = 0xFF00D4FF,
            glowIntensity = 0.91f,
            panelOpacity = 0.73f,
            presetName = "Ливень",
            overlayText = "текст / cue",
            overlayTextX = 18f,
            overlayTextY = 24f,
            overlayTextStart = 5f,
            overlayTextEnd = 18f,
            overlayTextAnimation = "Slide",
            layers = listOf(
                AtmosphereLayer(
                    id = "text-custom",
                    type = AtmosphereLayerType.Text,
                    name = "Custom text",
                    startTime = 5f,
                    endTime = 18f,
                    assetRef = "text.asset",
                    x = 18f,
                    y = 24f,
                    scale = 1.2f,
                    opacity = 0.82f,
                    text = "Привет",
                    accentColor = 0xFFFF4D8D,
                    animationIn = "Slide",
                    animationOut = "Fade",
                    isVisible = false
                )
            )
        )

        val decoded = AtmosphereConfigSerializer.decode(
            AtmosphereConfigSerializer.encode(config)
        )

        assertEquals(config, decoded)
    }

    @Test
    fun decode_returnsNullForUnsupportedVersion() {
        assertNull(AtmosphereConfigSerializer.decode("version=999"))
    }
}
