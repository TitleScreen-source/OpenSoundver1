package com.opensound.app.state

import com.opensound.app.models.Track
import com.opensound.app.playback.PlaybackSeekRequest

sealed class AudMoraPlaybackAction {
    data class TrackSelectedForPlayback(val track: Track) : AudMoraPlaybackAction()
    data object PlaybackToggled : AudMoraPlaybackAction()
    data class PlaybackProgressChanged(val seconds: Float) : AudMoraPlaybackAction()
    data class PlaybackSeekRequested(val seconds: Float) : AudMoraPlaybackAction()
    data object PlaybackPreviousRequested : AudMoraPlaybackAction()
    data object PlaybackNextRequested : AudMoraPlaybackAction()
    data object PlaybackCompleted : AudMoraPlaybackAction()
}

fun reduceAudMoraPlaybackState(
    state: AudMoraUiState,
    action: AudMoraPlaybackAction
): AudMoraUiState {
    return when (action) {
        is AudMoraPlaybackAction.TrackSelectedForPlayback -> {
            val nextQueue = state.playbackQueue.select(action.track)

            if (nextQueue.currentTrack.id == state.selectedTrack.id) {
                state.copy(isPlaying = true)
            } else {
                state.copy(
                    playbackQueue = nextQueue,
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
            playbackSeconds = clampPlaybackSeconds(
                seconds = action.seconds,
                state = state
            )
        )

        is AudMoraPlaybackAction.PlaybackSeekRequested -> {
            val safeSeconds = clampPlaybackSeconds(
                seconds = action.seconds,
                state = state
            )

            state.copy(
                playbackSeconds = safeSeconds,
                playbackSeekRequest = PlaybackSeekRequest(
                    id = (state.playbackSeekRequest?.id ?: 0L) + 1L,
                    seconds = safeSeconds
                )
            )
        }

        AudMoraPlaybackAction.PlaybackPreviousRequested -> moveToQueuedTrack(
            state = state,
            queue = state.playbackQueue.skipPrevious()
        )

        AudMoraPlaybackAction.PlaybackNextRequested -> moveToQueuedTrack(
            state = state,
            queue = state.playbackQueue.skipNext()
        )

        AudMoraPlaybackAction.PlaybackCompleted -> state.copy(
            isPlaying = false,
            playbackSeconds = 0f,
            playbackSeekRequest = null
        )
    }
}

private fun moveToQueuedTrack(
    state: AudMoraUiState,
    queue: PlaybackQueue
): AudMoraUiState {
    return if (queue.currentTrack.id == state.selectedTrack.id) {
        state
    } else {
        state.copy(
            playbackQueue = queue,
            playbackSeconds = 0f,
            playbackSeekRequest = null
        )
    }
}

private fun clampPlaybackSeconds(
    seconds: Float,
    state: AudMoraUiState
): Float {
    val durationSeconds = state.selectedTrack.durationSeconds.coerceAtLeast(0f)
    return if (durationSeconds == 0f) {
        0f
    } else {
        seconds.coerceIn(0f, durationSeconds)
    }
}
