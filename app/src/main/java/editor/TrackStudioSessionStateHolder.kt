package com.opensound.app.editor

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId
import com.opensound.app.models.limitAtmosphereConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackStudioSessionStateHolder(
    initialState: TrackStudioEditorState = TrackStudioEditorState(
        draftConfig = AtmosphereConfig()
    )
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<TrackStudioEditorState> = _state.asStateFlow()

    private var editingTrackId: TrackId? = null

    fun startEditing(
        trackId: TrackId,
        initialConfig: AtmosphereConfig
    ) {
        editingTrackId = trackId
        _state.value = TrackStudioEditorState(
            draftConfig = initialConfig,
            savedConfig = initialConfig
        )
    }

    fun dispatch(action: TrackStudioEditorAction) {
        _state.update { state ->
            reduceTrackStudioEditorState(state, action)
        }
    }

    fun saveConfig(): AtmosphereConfig {
        return limitAtmosphereConfig(_state.value.draftConfig)
    }

    fun markSaved() {
        markSaved(saveConfig())
    }

    fun markSaved(savedConfig: AtmosphereConfig) {
        _state.update { state ->
            state.copy(
                draftConfig = savedConfig,
                savedConfig = savedConfig,
                closeConfirmationVisible = false
            )
        }
    }

    fun requestClose(): Boolean {
        if (!_state.value.isDirty) {
            return true
        }

        _state.update { state ->
            state.copy(closeConfirmationVisible = true)
        }
        return false
    }

    fun dismissCloseConfirmation() {
        _state.update { state ->
            state.copy(closeConfirmationVisible = false)
        }
    }

    fun discardChanges() {
        _state.update { state ->
            state.copy(
                draftConfig = state.savedConfig,
                closeConfirmationVisible = false
            )
        }
    }

    fun isEditing(trackId: TrackId): Boolean {
        return editingTrackId == trackId
    }
}
