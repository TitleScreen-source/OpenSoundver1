package com.opensound.app.screens

import com.opensound.app.editor.TrackStudioEditorAction
import com.opensound.app.editor.TrackStudioEditorState
import com.opensound.app.models.AtmosphereConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackStudioStateHolderTest {
    @Test
    fun dispatch_updatesStateThroughReducer() {
        val holder = TrackStudioStateHolder(
            initialState = TrackStudioEditorState(draftConfig = AtmosphereConfig())
        )

        holder.dispatch(TrackStudioEditorAction.PreviewTimeChanged(64f))

        assertEquals(64f, holder.state.previewTimeSeconds, 0.001f)
    }

    @Test
    fun saveConfig_returnsLimitedDraftConfig() {
        val holder = TrackStudioStateHolder(
            initialState = TrackStudioEditorState(
                draftConfig = AtmosphereConfig(characterSize = 240f)
            )
        )

        val savedConfig = holder.saveConfig()

        assertEquals(150f, savedConfig.characterSize, 0.001f)
    }
}
