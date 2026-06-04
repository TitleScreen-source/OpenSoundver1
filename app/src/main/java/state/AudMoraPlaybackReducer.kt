package com.opensound.app.state

import com.opensound.app.models.Track
import com.opensound.app.playback.PlaybackEvent
import com.opensound.app.playback.PlaybackLoadState
import com.opensound.app.playback.PlaybackSeekRequest

sealed class AudMoraPlaybackAction {
    data class TrackSelectedForPlayback(
        val track: Track,
        val queueTracks: List<Track> = listOf(track),
        val queueSource: PlaybackQueueSource = PlaybackQueueSource.Catalog
    ) : AudMoraPlaybackAction()
    data object PlaybackToggled : AudMoraPlaybackAction()
    data class PlaybackProgressChanged(val seconds: Float) : AudMoraPlaybackAction()
    data class PlaybackSeekRequested(val seconds: Float) : AudMoraPlaybackAction()
    data object PlaybackPreviousRequested : AudMoraPlaybackAction()
    data object PlaybackNextRequested : AudMoraPlaybackAction()
    data object PlaybackShuffleToggled : AudMoraPlaybackAction()
    data object PlaybackRepeatModeCycled : AudMoraPlaybackAction()
    data object PlaybackCompleted : AudMoraPlaybackAction()
    data class PlaybackEventReceived(val event: PlaybackEvent) : AudMoraPlaybackAction()
}

fun reduceAudMoraPlaybackState(
    state: AudMoraUiState,
    action: AudMoraPlaybackAction
): AudMoraUiState {
    return when (action) {
        is AudMoraPlaybackAction.TrackSelectedForPlayback -> {
            val nextQueue = state.playbackQueue.replaceContext(
                track = action.track,
                queueTracks = action.queueTracks,
                source = action.queueSource
            )

            if (nextQueue.currentTrack.id == state.selectedTrack.id) {
                state.copy(
                    playbackQueue = nextQueue,
                    isPlaying = true,
                    playbackLoadState = if (state.playbackLoadState == PlaybackLoadState.Error) {
                        PlaybackLoadState.Buffering
                    } else {
                        state.playbackLoadState
                    },
                    playbackError = null
                )
            } else {
                state.copy(
                    playbackQueue = nextQueue,
                    isPlaying = true,
                    playbackSeconds = 0f,
                    playbackSeekRequest = null,
                    playbackLoadState = PlaybackLoadState.Buffering,
                    playbackError = null
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

        AudMoraPlaybackAction.PlaybackShuffleToggled -> state.copy(
            playbackQueue = state.playbackQueue.toggleShuffle()
        )

        AudMoraPlaybackAction.PlaybackRepeatModeCycled -> state.copy(
            playbackQueue = state.playbackQueue.cycleRepeatMode()
        )

        AudMoraPlaybackAction.PlaybackCompleted -> completePlayback(state)

        is AudMoraPlaybackAction.PlaybackEventReceived -> reducePlaybackEvent(
            state = state,
            event = action.event
        )
    }
}

private fun reducePlaybackEvent(
    state: AudMoraUiState,
    event: PlaybackEvent
): AudMoraUiState {
    return when (event) {
        PlaybackEvent.Ready -> state.copy(
            playbackLoadState = PlaybackLoadState.Ready,
            playbackError = null
        )

        PlaybackEvent.Buffering -> state.copy(
            playbackLoadState = PlaybackLoadState.Buffering
        )

        PlaybackEvent.Completed -> completePlayback(state)

        is PlaybackEvent.Error -> state.copy(
            isPlaying = false,
            playbackLoadState = PlaybackLoadState.Error,
            playbackError = event.error
        )
    }
}

private fun completePlayback(state: AudMoraUiState): AudMoraUiState {
    if (state.playbackQueue.repeatMode == PlaybackRepeatMode.One) {
        return restartCurrentTrack(state)
    }

    val nextQueue = state.playbackQueue.skipNext()
    if (nextQueue.currentTrack.id != state.selectedTrack.id) {
        return state.copy(
            playbackQueue = nextQueue,
            isPlaying = true,
            playbackSeconds = 0f,
            playbackSeekRequest = null,
            playbackLoadState = PlaybackLoadState.Buffering,
            playbackError = null
        )
    }

    if (state.playbackQueue.repeatMode == PlaybackRepeatMode.All) {
        return restartCurrentTrack(state)
    }

    return state.copy(
        isPlaying = false,
        playbackSeconds = 0f,
        playbackSeekRequest = null,
        playbackLoadState = PlaybackLoadState.Idle
    )
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
            playbackSeekRequest = null,
            playbackLoadState = PlaybackLoadState.Buffering,
            playbackError = null
        )
    }
}

private fun restartCurrentTrack(state: AudMoraUiState): AudMoraUiState {
    return state.copy(
        isPlaying = true,
        playbackSeconds = 0f,
        playbackSeekRequest = PlaybackSeekRequest(
            id = (state.playbackSeekRequest?.id ?: 0L) + 1L,
            seconds = 0f
        ),
        playbackLoadState = PlaybackLoadState.Ready,
        playbackError = null
    )
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
