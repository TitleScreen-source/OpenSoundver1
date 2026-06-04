package com.opensound.app.playback

class PlaybackController(
    val mediaItem: PlaybackMediaItem,
    private val engine: AudioPlaybackEngine
) {
    val isPlaying: Boolean
        get() = engine.isPlaying

    val currentPositionSeconds: Float
        get() = engine.currentPositionSeconds

    fun synchronize(shouldPlay: Boolean) {
        when {
            shouldPlay && !engine.isPlaying -> engine.play()
            !shouldPlay && engine.isPlaying -> engine.pause()
        }
    }

    fun seekTo(seconds: Float): Float {
        engine.seekTo(seconds)
        return engine.currentPositionSeconds
    }

    fun setOnEvent(onEvent: (PlaybackEvent) -> Unit) {
        engine.setOnEvent { event ->
            if (event == PlaybackEvent.Completed) {
                engine.seekToStart()
            }
            onEvent(event)
        }
    }

    fun release() {
        engine.setOnEvent(null)
        engine.release()
    }
}

fun interface PlaybackControllerFactory {
    fun create(mediaItem: PlaybackMediaItem): PlaybackController
}

class DefaultPlaybackControllerFactory(
    private val engineFactory: AudioPlaybackEngineFactory
) : PlaybackControllerFactory {
    override fun create(mediaItem: PlaybackMediaItem): PlaybackController {
        return PlaybackController(
            mediaItem = mediaItem,
            engine = engineFactory.create(mediaItem)
        )
    }
}
