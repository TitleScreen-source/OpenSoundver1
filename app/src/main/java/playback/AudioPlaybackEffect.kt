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
    val playbackControllerFactory = remember(context) {
        DefaultPlaybackControllerFactory(
            engineFactory = AndroidAudioPlaybackEngineFactory(context)
        )
    }
    val playbackController = remember(mediaItem, playbackControllerFactory) {
        playbackControllerFactory.create(mediaItem)
    }
    val latestPlaybackSecondsChanged by rememberUpdatedState(onPlaybackSecondsChanged)
    val latestPlaybackCompleted by rememberUpdatedState(onPlaybackCompleted)

    DisposableEffect(playbackController) {
        playbackController.setOnCompletion(latestPlaybackCompleted)

        onDispose {
            playbackController.release()
        }
    }

    LaunchedEffect(playbackController, seekRequest) {
        seekRequest?.let { request ->
            latestPlaybackSecondsChanged(playbackController.seekTo(request.seconds))
            playbackController.synchronize(shouldPlay = isPlaying)
        }
    }

    LaunchedEffect(playbackController, isPlaying) {
        playbackController.synchronize(shouldPlay = isPlaying)

        while (isPlaying) {
            latestPlaybackSecondsChanged(playbackController.currentPositionSeconds)
            delay(33L)
        }
    }
}
