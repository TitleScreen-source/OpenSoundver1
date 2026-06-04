package com.opensound.app.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun AudioPlaybackEffect(
    mediaItem: PlaybackMediaItem,
    isPlaying: Boolean,
    seekRequest: PlaybackSeekRequest?,
    onPlaybackSecondsChanged: (Float) -> Unit,
    onPlaybackCompleted: () -> Unit
) {
    val context = LocalContext.current
    val playbackEngineFactory = remember(context) {
        AndroidAudioPlaybackEngineFactory(context)
    }
    val playbackEngine = remember(mediaItem, playbackEngineFactory) {
        playbackEngineFactory.create(mediaItem)
    }
    val latestPlaybackSecondsChanged by rememberUpdatedState(onPlaybackSecondsChanged)
    val latestPlaybackCompleted by rememberUpdatedState(onPlaybackCompleted)

    DisposableEffect(playbackEngine) {
        playbackEngine.setOnCompletion {
            handlePlaybackCompletion(
                engine = playbackEngine,
                onPlaybackCompleted = latestPlaybackCompleted
            )
        }

        onDispose {
            playbackEngine.release()
        }
    }

    LaunchedEffect(playbackEngine, seekRequest) {
        seekRequest?.let { request ->
            playbackEngine.seekTo(request.seconds)
            latestPlaybackSecondsChanged(playbackEngine.currentPositionSeconds)
            synchronizePlayback(
                engine = playbackEngine,
                shouldPlay = isPlaying
            )
        }
    }

    LaunchedEffect(playbackEngine, isPlaying) {
        synchronizePlayback(
            engine = playbackEngine,
            shouldPlay = isPlaying
        )

        while (isPlaying) {
            latestPlaybackSecondsChanged(playbackEngine.currentPositionSeconds)
            delay(33L)
        }
    }
}
