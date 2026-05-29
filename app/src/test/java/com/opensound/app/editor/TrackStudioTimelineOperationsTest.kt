package com.opensound.app.editor

import com.opensound.app.editor.TrackStudioLayerIds.TEXT_MAIN_LAYER_ID
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackStudioTimelineOperationsTest {
    @Test
    fun deleteSelectedLayer_refusesProtectedPrimaryLayers() {
        val result = deleteSelectedLayer(
            config = AtmosphereConfig(),
            selectedLayerId = TEXT_MAIN_LAYER_ID
        )

        assertNull(result)
        assertTrue(isProtectedLayer(TEXT_MAIN_LAYER_ID))
    }

    @Test
    fun addTimelineLayer_createsTypedLayerAtPlayheadAndSelectsIt() {
        val result = addTimelineLayer(
            config = AtmosphereConfig(),
            type = AtmosphereLayerType.Text,
            playheadSeconds = 12f
        )
        val config = result.first
        val selectedLayerId = result.second
        val addedLayer = config.layers.first { layer -> layer.id == selectedLayerId }

        assertEquals("text-2", selectedLayerId)
        assertEquals(AtmosphereLayerType.Text, addedLayer.type)
        assertEquals(12f, addedLayer.startTime, 0.001f)
        assertEquals(24f, addedLayer.endTime, 0.001f)
    }

    @Test
    fun moveLayerToStart_preservesDurationAndClampsToTimelineEnd() {
        val layer = testLayer(startTime = 80f, endTime = 100f)

        val moved = moveLayerToStart(layer = layer, startTime = 96f)

        assertEquals(80f, moved.startTime, 0.001f)
        assertEquals(100f, moved.endTime, 0.001f)
    }

    @Test
    fun trimLayerStartTo_keepsMinimumTwoSecondDuration() {
        val layer = testLayer(startTime = 10f, endTime = 20f)

        val trimmed = trimLayerStartTo(layer = layer, startTime = 19.5f)

        assertEquals(18f, trimmed.startTime, 0.001f)
        assertEquals(20f, trimmed.endTime, 0.001f)
    }

    @Test
    fun toggleLayerVisibility_flipsOnlyTargetLayer() {
        val config = AtmosphereConfig()
        val targetLayerId = config.layers.first().id

        val updated = toggleLayerVisibility(config = config, layerId = targetLayerId)

        assertFalse(updated.layers.first { layer -> layer.id == targetLayerId }.isVisible)
        assertTrue(updated.layers.drop(1).all { layer -> layer.isVisible })
    }

    @Test
    fun updateTimelineLayer_syncsPrimaryTextLayerBackToConfig() {
        val config = AtmosphereConfig()
        val textLayer = timelineLayersFor(config)
            .first { layer -> layer.id == TEXT_MAIN_LAYER_ID }
            .copy(
                text = "HELLO",
                startTime = 8f,
                endTime = 20f,
                x = 16f,
                y = 24f,
                animationIn = "Pulse"
            )

        val updated = updateTimelineLayer(config = config, updatedLayer = textLayer)

        assertEquals("HELLO", updated.overlayText)
        assertEquals(8f, updated.overlayTextStart, 0.001f)
        assertEquals(20f, updated.overlayTextEnd, 0.001f)
        assertEquals(16f, updated.overlayTextX, 0.001f)
        assertEquals(24f, updated.overlayTextY, 0.001f)
        assertEquals("Pulse", updated.overlayTextAnimation)
    }

    @Test
    fun duplicateSelectedLayer_offsetsCopyAndReturnsNewSelection() {
        val addResult = addTimelineLayer(
            config = AtmosphereConfig(),
            type = AtmosphereLayerType.Effect,
            playheadSeconds = 20f
        )
        val duplicateResult = duplicateSelectedLayer(
            config = addResult.first,
            selectedLayerId = addResult.second
        )

        requireNotNull(duplicateResult)
        val duplicatedLayer = duplicateResult.first.layers
            .first { layer -> layer.id == duplicateResult.second }

        assertEquals("${addResult.second}-copy-1", duplicateResult.second)
        assertEquals(24f, duplicatedLayer.startTime, 0.001f)
        assertEquals(34f, duplicatedLayer.endTime, 0.001f)
    }

    private fun testLayer(
        startTime: Float,
        endTime: Float
    ): AtmosphereLayer {
        return AtmosphereLayer(
            id = "test-layer",
            type = AtmosphereLayerType.Effect,
            name = "Test",
            startTime = startTime,
            endTime = endTime
        )
    }
}
