package com.opensound.app.editor

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrackStudioEditorStateTest {
    @Test
    fun editorState_defaultsToSceneSectionAndMainTextLayer() {
        val state = TrackStudioEditorState(draftConfig = AtmosphereConfig())

        assertEquals(TrackStudioSection.Scene, state.selectedSection)
        assertEquals(24f, state.previewTimeSeconds, 0.001f)
        assertEquals(TrackStudioLayerIds.TEXT_MAIN_LAYER_ID, state.selectedLayerId)
        assertEquals(false, state.isDirty)
    }

    @Test
    fun editorState_isDirtyWhenDraftDiffersFromSavedConfig() {
        val state = TrackStudioEditorState(
            draftConfig = AtmosphereConfig(presetName = "Draft"),
            savedConfig = AtmosphereConfig(presetName = "Saved")
        )

        assertEquals(true, state.isDirty)
    }

    @Test
    fun sectionForLayerType_routesLayerTypesToEditorSections() {
        assertEquals(
            TrackStudioSection.Character,
            sectionForLayerType(AtmosphereLayerType.Character)
        )
        assertEquals(
            TrackStudioSection.Text,
            sectionForLayerType(AtmosphereLayerType.Text)
        )
        assertEquals(
            TrackStudioSection.Scene,
            sectionForLayerType(AtmosphereLayerType.Effect)
        )
        assertEquals(
            TrackStudioSection.Assets,
            sectionForLayerType(AtmosphereLayerType.Background)
        )
        assertEquals(
            TrackStudioSection.Timing,
            sectionForLayerType(AtmosphereLayerType.Wave)
        )
    }

    @Test
    fun sectionFromLabel_fallsBackToSceneForUnknownLabels() {
        assertEquals(TrackStudioSection.Text, TrackStudioSection.fromLabel("Text"))
        assertEquals(TrackStudioSection.Scene, TrackStudioSection.fromLabel("Unknown"))
    }

    @Test
    fun onlyDraggableSectionsExposeEditorDragModes() {
        assertEquals("Character", TrackStudioSection.Character.editorDragMode)
        assertEquals("Text", TrackStudioSection.Text.editorDragMode)
        assertNull(TrackStudioSection.Scene.editorDragMode)
        assertNull(TrackStudioSection.Timing.editorDragMode)
        assertNull(TrackStudioSection.Assets.editorDragMode)
    }
}
