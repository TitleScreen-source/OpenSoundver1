package com.opensound.app.playback

import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackMediaItemTest {
    @Test
    fun toPlaybackMediaItem_mapsTrackMetadataAndResolvedAudioSource() {
        val track = Track(
            id = TrackId("track"),
            title = "Night Signal",
            artist = "AudMora Artist",
            audioSource = TrackAudioSource.LocalRawResource(resId = 7),
            durationSeconds = 209f
        )
        val resolvedAudioSource = TrackAudioSource.LocalRawResource(resId = 700)

        val mediaItem = track.toPlaybackMediaItem(
            audioSource = resolvedAudioSource
        )

        assertEquals(track.id, mediaItem.id)
        assertEquals(track.title, mediaItem.title)
        assertEquals(track.artist, mediaItem.artist)
        assertEquals(track.durationSeconds, mediaItem.durationSeconds, 0.001f)
        assertEquals(resolvedAudioSource, mediaItem.audioSource)
    }
}
