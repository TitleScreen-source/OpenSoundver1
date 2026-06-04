package com.opensound.app.playback

data class PlaybackError(
    val code: String,
    val message: String
)

enum class PlaybackLoadState {
    Idle,
    Buffering,
    Ready,
    Error
}

sealed class PlaybackEvent {
    data object Ready : PlaybackEvent()
    data object Buffering : PlaybackEvent()
    data object Completed : PlaybackEvent()
    data class Error(val error: PlaybackError) : PlaybackEvent()
}
