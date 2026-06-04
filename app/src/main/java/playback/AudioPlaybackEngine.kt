package com.opensound.app.playback

interface AudioPlaybackEngine {
    val isPlaying: Boolean
    val currentPositionSeconds: Float

    fun play()
    fun pause()
    fun seekTo(seconds: Float)
    fun seekToStart() {
        seekTo(0f)
    }
    fun release()
    fun setOnEvent(onEvent: ((PlaybackEvent) -> Unit)?)
}
