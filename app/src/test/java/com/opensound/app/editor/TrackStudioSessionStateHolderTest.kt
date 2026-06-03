package com.opensound.app.editor

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackStudioSessionStateHolderTest {
    @Test
    fun startEditing_replacesDraftStateForTrack() {
        val trackId = TrackId("track")
        val config = AtmosphereConfig(presetName = "Track draft")
        val holder = TrackStudioSessionStateHolder()

        holder.startEditing(
            trackId = trackId,
            initialConfig = config
        )

        assertTrue(holder.isEditing(trackId))
        assertEquals(config, holder.state.value.draftConfig)
        assertEquals(config, holder.state.value.savedConfig)
        assertFalse(holder.state.value.isDirty)
    }

    @Test
    fun dispatch_updatesStateThroughReducer() {
        val holder = TrackStudioSessionStateHolder(
            initialState = TrackStudioEditorState(draftConfig = AtmosphereConfig())
        )

        holder.dispatch(TrackStudioEditorAction.PreviewTimeChanged(64f))

        assertEquals(64f, holder.state.value.previewTimeSeconds, 0.001f)
    }

    @Test
    fun saveConfig_returnsLimitedDraftConfig() {
        val holder = TrackStudioSessionStateHolder(
            initialState = TrackStudioEditorState(
                draftConfig = AtmosphereConfig(characterSize = 240f)
            )
        )

        val savedConfig = holder.saveConfig()

        assertEquals(150f, savedConfig.characterSize, 0.001f)
    }

    @Test
    fun isEditing_returnsFalseForOtherTrack() {
        val holder = TrackStudioSessionStateHolder()

        holder.startEditing(
            trackId = TrackId("current"),
            initialConfig = AtmosphereConfig()
        )

        assertFalse(holder.isEditing(TrackId("other")))
    }

    @Test
    fun requestClose_returnsTrueWhenDraftIsClean() {
        val holder = TrackStudioSessionStateHolder()

        holder.startEditing(
            trackId = TrackId("track"),
            initialConfig = AtmosphereConfig()
        )

        assertTrue(holder.requestClose())
        assertFalse(holder.state.value.closeConfirmationVisible)
    }

    @Test
    fun requestClose_showsConfirmationWhenDraftIsDirty() {
        val holder = TrackStudioSessionStateHolder()

        holder.startEditing(
            trackId = TrackId("track"),
            initialConfig = AtmosphereConfig()
        )
        holder.dispatch(
            TrackStudioEditorAction.DraftConfigChanged(
                AtmosphereConfig(presetName = "Changed")
            )
        )

        assertFalse(holder.requestClose())
        assertTrue(holder.state.value.closeConfirmationVisible)
    }

    @Test
    fun discardChanges_restoresSavedConfigAndHidesConfirmation() {
        val savedConfig = AtmosphereConfig(presetName = "Saved")
        val holder = TrackStudioSessionStateHolder()

        holder.startEditing(
            trackId = TrackId("track"),
            initialConfig = savedConfig
        )
        holder.dispatch(
            TrackStudioEditorAction.DraftConfigChanged(
                AtmosphereConfig(presetName = "Changed")
            )
        )
        holder.requestClose()

        holder.discardChanges()

        assertEquals(savedConfig, holder.state.value.draftConfig)
        assertFalse(holder.state.value.isDirty)
        assertFalse(holder.state.value.closeConfirmationVisible)
    }

    @Test
    fun markSaved_updatesSavedConfigAndClearsDirtyState() {
        val holder = TrackStudioSessionStateHolder()

        holder.startEditing(
            trackId = TrackId("track"),
            initialConfig = AtmosphereConfig()
        )
        holder.dispatch(
            TrackStudioEditorAction.DraftConfigChanged(
                AtmosphereConfig(presetName = "Changed")
            )
        )

        holder.markSaved()

        assertEquals(holder.state.value.draftConfig, holder.state.value.savedConfig)
        assertFalse(holder.state.value.isDirty)
    }
}
