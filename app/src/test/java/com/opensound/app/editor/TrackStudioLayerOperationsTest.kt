package com.opensound.app.editor

import com.opensound.app.editor.TrackStudioLayerIds.TEXT_MAIN_LAYER_ID
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TrackStudioLayerOperationsTest {
    @Test
    fun selectedLayerFor_fallsBackToPrimaryLayerWhenSelectedIdDoesNotMatchType() {
        val layer = selectedLayerFor(
            config = AtmosphereConfig(),
            selectedLayerId = "missing",
            type = AtmosphereLayerType.Text
        )

        assertNotNull(layer)
        assertEquals(TEXT_MAIN_LAYER_ID, layer?.id)
    }

    @Test
    fun moveSelectedLayer_clampsPrimaryTextPosition() {
        val moved = moveSelectedLayer(
            config = AtmosphereConfig(),
            selectedLayerId = TEXT_MAIN_LAYER_ID,
            type = AtmosphereLayerType.Text,
            dx = 400f,
            dy = 400f
        )

        assertEquals(240f, moved.overlayTextX, 0.001f)
        assertEquals(105f, moved.overlayTextY, 0.001f)
    }

    @Test
    fun updateSelectedText_truncatesPrimaryTextCue() {
        val updated = updateSelectedText(
            config = AtmosphereConfig(),
            selectedLayerId = TEXT_MAIN_LAYER_ID,
            text = "THIS TEXT IS LONGER THAN THE EDITOR LIMIT"
        )

        assertEquals(28, updated.overlayText.length)
        assertEquals("THIS TEXT IS LONGER THAN THE", updated.overlayText)
    }

    @Test
    fun updateSelectedText_updatesSecondaryTextLayerWithoutChangingPrimaryOverlay() {
        val addResult = addTimelineLayer(
            config = AtmosphereConfig(overlayText = "PRIMARY"),
            type = AtmosphereLayerType.Text,
            playheadSeconds = 12f
        )

        val updated = updateSelectedText(
            config = addResult.first,
            selectedLayerId = addResult.second,
            text = "SECONDARY"
        )
        val secondaryLayer = updated.layers.first { layer -> layer.id == addResult.second }

        assertEquals("PRIMARY", updated.overlayText)
        assertEquals("SECONDARY", secondaryLayer.text)
        assertEquals("SECONDARY", secondaryLayer.name)
    }

    @Test
    fun updateSelectedCharacterSize_clampsPrimaryCharacterSize() {
        val updated = updateSelectedCharacterSize(
            config = AtmosphereConfig(),
            selectedLayerId = TrackStudioLayerIds.CHARACTER_MAIN_LAYER_ID,
            size = 240f
        )

        assertEquals(150f, updated.characterSize, 0.001f)
    }

    @Test
    fun updateSelectedTextAnimation_updatesSecondaryTextLayerOnly() {
        val addResult = addTimelineLayer(
            config = AtmosphereConfig(overlayTextAnimation = "Fade"),
            type = AtmosphereLayerType.Text,
            playheadSeconds = 12f
        )

        val updated = updateSelectedTextAnimation(
            config = addResult.first,
            selectedLayerId = addResult.second,
            animation = "Pulse"
        )
        val secondaryLayer = updated.layers.first { layer -> layer.id == addResult.second }

        assertEquals("Fade", updated.overlayTextAnimation)
        assertEquals("Pulse", secondaryLayer.animationIn)
    }
}
