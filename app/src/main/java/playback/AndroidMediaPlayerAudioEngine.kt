package com.opensound.app.playback

import android.content.Context
import android.media.MediaPlayer
import kotlin.math.roundToInt

class AndroidMediaPlayerAudioEngine private constructor(
    private val mediaPlayer: MediaPlayer
) : AudioPlaybackEngine {
    override val isPlaying: Boolean
        get() = runCatching { mediaPlayer.isPlaying }.getOrDefault(false)

    override val currentPositionSeconds: Float
        get() = runCatching { mediaPlayer.currentPosition / 1000f }.getOrDefault(0f)

    override fun play() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun seekTo(seconds: Float) {
        val positionMillis = (seconds.coerceAtLeast(0f) * 1000f).roundToInt()
        mediaPlayer.seekTo(positionMillis)
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun setOnEvent(onEvent: ((PlaybackEvent) -> Unit)?) {
        if (onEvent == null) {
            mediaPlayer.setOnCompletionListener(null)
            mediaPlayer.setOnErrorListener(null)
            return
        }

        mediaPlayer.setOnCompletionListener {
            onEvent(PlaybackEvent.Completed)
        }
        mediaPlayer.setOnErrorListener { _, what, extra ->
            onEvent(
                PlaybackEvent.Error(
                    PlaybackError(
                        code = "media_player_$what",
                        message = "MediaPlayer error: what=$what, extra=$extra"
                    )
                )
            )
            true
        }
        onEvent(PlaybackEvent.Ready)
    }

    companion object {
        fun create(
            context: Context,
            audioResId: Int
        ): AndroidMediaPlayerAudioEngine {
            return AndroidMediaPlayerAudioEngine(
                mediaPlayer = MediaPlayer.create(context, audioResId)
            )
        }
    }
}
