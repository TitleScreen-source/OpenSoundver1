package com.opensound.app.playback

interface AudioPlaybackEngine {
    val isPlaying: Boolean
    val currentPositionSeconds: Float

    fun play()
    fun pause()
    fun seekToStart()
    fun release()
    fun setOnCompletion(onCompleted: (() -> Unit)?)
}

fun synchronizePlayback(
    engine: AudioPlaybackEngine,
    shouldPlay: Boolean
) {
    when {
        shouldPlay && !engine.isPlaying -> engine.play()
        !shouldPlay && engine.isPlaying -> engine.pause()
    }
}

fun handlePlaybackCompletion(
    engine: AudioPlaybackEngine,
    onPlaybackCompleted: () -> Unit
) {
    engine.seekToStart()
    onPlaybackCompleted()
}
