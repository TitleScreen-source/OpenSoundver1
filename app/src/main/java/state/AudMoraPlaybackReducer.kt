package com.opensound.app.state

import com.opensound.app.models.Track
import com.opensound.app.playback.PlaybackSeekRequest

sealed class AudMoraPlaybackAction {
    data class TrackSelectedForPlayback(val track: Track) : AudMoraPlaybackAction()
    data object PlaybackToggled : AudMoraPlaybackAction()
    data class PlaybackProgressChanged(val seconds: Float) : AudMoraPlaybackAction()
    data class PlaybackSeekRequested(val seconds: Float) : AudMoraPlaybackAction()
    data object PlaybackCompleted : AudMoraPlaybackAction()
}

fun reduceAudMoraPlaybackState(
    state: AudMoraUiState,
    action: AudMoraPlaybackAction
): AudMoraUiState {
    return when (action) {
        is AudMoraPlaybackAction.TrackSelectedForPlayback -> {
            if (action.track == state.selectedTrack) {
                state.copy(isPlaying = true)
            } else {
                state.copy(
                    selectedTrack = action.track,
                    isPlaying = true,
                    playbackSeconds = 0f,
                    playbackSeekRequest = null
                )
            }
        }

        AudMoraPlaybackAction.PlaybackToggled -> state.copy(
            isPlaying = !state.isPlaying
        )

        is AudMoraPlaybackAction.PlaybackProgressChanged -> state.copy(
            playbackSeconds = action.seconds.coerceAtLeast(0f)
        )

        is AudMoraPlaybackAction.PlaybackSeekRequested -> {
            val safeSeconds = action.seconds.coerceAtLeast(0f)

            state.copy(
                playbackSeconds = safeSeconds,
                playbackSeekRequest = PlaybackSeekRequest(
                    id = (state.playbackSeekRequest?.id ?: 0L) + 1L,
                    seconds = safeSeconds
                )
            )
        }

        AudMoraPlaybackAction.PlaybackCompleted -> state.copy(
            isPlaying = false,
            playbackSeconds = 0f,
            playbackSeekRequest = null
        )
    }
}
