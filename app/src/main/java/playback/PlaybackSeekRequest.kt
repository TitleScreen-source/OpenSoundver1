package com.opensound.app.playback

data class PlaybackSeekRequest(
    val id: Long,
    val seconds: Float
)
