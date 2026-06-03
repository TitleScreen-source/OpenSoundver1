package com.opensound.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.opensound.app.editor.TrackStudioEditorAction
import com.opensound.app.editor.TrackStudioEditorState
import com.opensound.app.editor.reduceTrackStudioEditorState
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId
import com.opensound.app.models.limitAtmosphereConfig

internal class TrackStudioStateHolder(
    initialState: TrackStudioEditorState
) {
    var state by mutableStateOf(initialState)
        private set

    fun dispatch(action: TrackStudioEditorAction) {
        state = reduceTrackStudioEditorState(state, action)
    }

    fun saveConfig(): AtmosphereConfig {
        return limitAtmosphereConfig(state.draftConfig)
    }
}

@Composable
internal fun rememberTrackStudioStateHolder(
    trackId: TrackId,
    initialConfig: AtmosphereConfig
): TrackStudioStateHolder {
    return remember(trackId) {
        TrackStudioStateHolder(
            initialState = TrackStudioEditorState(draftConfig = initialConfig)
        )
    }
}
