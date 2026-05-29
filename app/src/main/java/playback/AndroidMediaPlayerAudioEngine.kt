package com.opensound.app.playback

import android.content.Context
import android.media.MediaPlayer

class AndroidMediaPlayerAudioEngine private constructor(
    private val mediaPlayer: MediaPlayer
) : AudioPlaybackEngine {
    override val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    override val currentPositionSeconds: Float
        get() = mediaPlayer.currentPosition / 1000f

    override fun play() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun seekToStart() {
        mediaPlayer.seekTo(0)
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun setOnCompletion(onCompleted: (() -> Unit)?) {
        mediaPlayer.setOnCompletionListener {
            onCompleted?.invoke()
        }
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
