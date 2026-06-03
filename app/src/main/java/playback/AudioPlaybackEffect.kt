package com.opensound.app.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.opensound.app.models.TrackAudioSource
import kotlinx.coroutines.delay

@Composable
fun AudioPlaybackEffect(
    audioSource: TrackAudioSource,
    isPlaying: Boolean,
    onPlaybackSecondsChanged: (Float) -> Unit,
    onPlaybackCompleted: () -> Unit
) {
    val context = LocalContext.current
    val playbackEngine = remember(audioSource) {
        when (audioSource) {
            is TrackAudioSource.LocalRawResource -> AndroidMediaPlayerAudioEngine.create(
                context = context,
                audioResId = audioSource.resId
            )
        }
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
