package com.opensound.app.editor

import com.opensound.app.editor.TrackStudioLayerIds.CHARACTER_MAIN_LAYER_ID
import com.opensound.app.editor.TrackStudioLayerIds.TEXT_MAIN_LAYER_ID
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TrackStudioEditorReducerTest {
    @Test
    fun layerAdded_selectsNewLayerAndMatchingEditorSection() {
        val state = TrackStudioEditorState(
            draftConfig = AtmosphereConfig(),
            previewTimeSeconds = 12f
        )

        val next = reduceTrackStudioEditorState(
            state = state,
            action = TrackStudioEditorAction.LayerAdded(AtmosphereLayerType.Text)
        )
        val selectedLayer = next.draftConfig.layers.first { layer -> layer.id == next.selectedLayerId }

        assertEquals("text-2", next.selectedLayerId)
        assertEquals(TrackStudioSection.Text, next.selectedSection)
        assertEquals(12f, selectedLayer.startTime, 0.001f)
    }

    @Test
    fun selectedLayerDeleted_keepsStateWhenLayerIsProtected() {
        val state = TrackStudioEditorState(
            draftConfig = AtmosphereConfig(),
            selectedLayerId = TEXT_MAIN_LAYER_ID,
            selectedSection = TrackStudioSection.Text
        )

        val next = reduceTrackStudioEditorState(
            state = state,
            action = TrackStudioEditorAction.SelectedLayerDeleted
        )

        assertEquals(state, next)
    }

    @Test
    fun draftConfigChanged_limitsValuesAndSyncsPrimaryLayers() {
        val next = reduceTrackStudioEditorState(
            state = TrackStudioEditorState(draftConfig = AtmosphereConfig()),
            action = TrackStudioEditorAction.DraftConfigChanged(
                AtmosphereConfig(
                    characterSize = 220f,
                    overlayText = "SYNC",
                    overlayTextX = 999f,
                    overlayTextY = 999f
                )
            )
        )
        val characterLayer = next.draftConfig.layers.first { layer -> layer.id == CHARACTER_MAIN_LAYER_ID }
        val textLayer = next.draftConfig.layers.first { layer -> layer.id == TEXT_MAIN_LAYER_ID }

        assertEquals(150f, next.draftConfig.characterSize, 0.001f)
        assertEquals(1.5f, characterLayer.scale, 0.001f)
        assertEquals("SYNC", textLayer.text)
        assertEquals(240f, textLayer.x, 0.001f)
        assertEquals(105f, textLayer.y, 0.001f)
    }

    @Test
    fun layerDragged_updatesSelectedPrimaryTextLayer() {
        val state = TrackStudioEditorState(
            draftConfig = AtmosphereConfig(),
            selectedLayerId = TEXT_MAIN_LAYER_ID,
            selectedSection = TrackStudioSection.Text
        )

        val next = reduceTrackStudioEditorState(
            state = state,
            action = TrackStudioEditorAction.LayerDragged(
                type = AtmosphereLayerType.Text,
                dx = 32f,
                dy = 12f
            )
        )
        val textLayer = next.draftConfig.layers.first { layer -> layer.id == TEXT_MAIN_LAYER_ID }

        assertEquals(32f, next.draftConfig.overlayTextX, 0.001f)
        assertEquals(6f, next.draftConfig.overlayTextY, 0.001f)
        assertEquals(32f, textLayer.x, 0.001f)
        assertEquals(6f, textLayer.y, 0.001f)
    }

    @Test
    fun layerVisibilityToggled_updatesOnlyTargetLayer() {
        val state = TrackStudioEditorState(draftConfig = AtmosphereConfig())

        val next = reduceTrackStudioEditorState(
            state = state,
            action = TrackStudioEditorAction.LayerVisibilityToggled(TEXT_MAIN_LAYER_ID)
        )

        assertFalse(next.draftConfig.layers.first { layer -> layer.id == TEXT_MAIN_LAYER_ID }.isVisible)
        assertEquals(
            state.draftConfig.layers.first { layer -> layer.id == CHARACTER_MAIN_LAYER_ID }.isVisible,
            next.draftConfig.layers.first { layer -> layer.id == CHARACTER_MAIN_LAYER_ID }.isVisible
        )
    }
}
