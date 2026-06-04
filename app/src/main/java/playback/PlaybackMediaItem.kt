package com.opensound.app.playback

import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId

data class PlaybackMediaItem(
    val id: TrackId,
    val title: String,
    val artist: String,
    val durationSeconds: Float,
    val audioSource: TrackAudioSource
)

fun Track.toPlaybackMediaItem(
    audioSource: TrackAudioSource = this.audioSource
): PlaybackMediaItem {
    return PlaybackMediaItem(
        id = id,
        title = title,
        artist = artist,
        durationSeconds = durationSeconds,
        audioSource = audioSource
    )
}
