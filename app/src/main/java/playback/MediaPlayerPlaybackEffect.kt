package com.opensound.app.playback

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun MediaPlayerPlaybackEffect(
    audioResId: Int,
    isPlaying: Boolean,
    onPlaybackSecondsChanged: (Float) -> Unit,
    onPlaybackCompleted: () -> Unit
) {
    val context = LocalContext.current
    val mediaPlayer = remember(audioResId) {
        MediaPlayer.create(context, audioResId)
    }

    DisposableEffect(mediaPlayer) {
        mediaPlayer.setOnCompletionListener { player ->
            player.seekTo(0)
            onPlaybackCompleted()
        }

        onDispose {
            mediaPlayer.release()
        }
    }

    LaunchedEffect(mediaPlayer, isPlaying) {
        if (isPlaying) {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }

            while (isPlaying) {
                onPlaybackSecondsChanged(mediaPlayer.currentPosition / 1000f)
                delay(33L)
            }
        } else if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }
}
