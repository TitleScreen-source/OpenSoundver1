package com.opensound.app.state

import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudMoraPlaybackReducerTest {
    @Test
    fun trackSelectedForPlayback_startsSameTrackWithoutRewinding() {
        val track = testTrack(id = "track-current")
        val state = testState(
            selectedTrack = track,
            isPlaying = false,
            playbackSeconds = 42f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.TrackSelectedForPlayback(track)
        )

        assertEquals(track, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(42f, next.playbackSeconds, 0.001f)
    }

    @Test
    fun trackSelectedForPlayback_switchesTrackAndRewindsProgress() {
        val currentTrack = testTrack(id = "track-current")
        val nextTrack = testTrack(id = "track-next")
        val state = testState(
            selectedTrack = currentTrack,
            isPlaying = true,
            playbackSeconds = 42f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.TrackSelectedForPlayback(nextTrack)
        )

        assertEquals(nextTrack, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
    }

    @Test
    fun playbackProgressChanged_clampsNegativeProgressToZero() {
        val state = testState()

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackProgressChanged(-5f)
        )

        assertEquals(0f, next.playbackSeconds, 0.001f)
    }

    @Test
    fun playbackToggled_flipsPlayingState() {
        val state = testState(isPlaying = false)

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackToggled
        )

        assertTrue(next.isPlaying)
    }

    @Test
    fun playbackSeekRequested_updatesProgressAndCreatesSeekRequest() {
        val state = testState(playbackSeconds = 12f)

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackSeekRequested(32f)
        )

        assertEquals(32f, next.playbackSeconds, 0.001f)
        assertEquals(1L, next.playbackSeekRequest?.id)
        assertEquals(32f, next.playbackSeekRequest?.seconds ?: -1f, 0.001f)
    }

    @Test
    fun playbackSeekRequested_incrementsRequestIdForRepeatedSamePositionSeek() {
        val first = reduceAudMoraPlaybackState(
            state = testState(),
            action = AudMoraPlaybackAction.PlaybackSeekRequested(32f)
        )

        val second = reduceAudMoraPlaybackState(
            state = first,
            action = AudMoraPlaybackAction.PlaybackSeekRequested(32f)
        )

        assertEquals(2L, second.playbackSeekRequest?.id)
        assertEquals(32f, second.playbackSeekRequest?.seconds ?: -1f, 0.001f)
    }

    @Test
    fun trackSelectedForPlayback_clearsPreviousSeekRequestWhenTrackChanges() {
        val currentTrack = testTrack(id = "track-current")
        val nextTrack = testTrack(id = "track-next")
        val state = reduceAudMoraPlaybackState(
            state = testState(selectedTrack = currentTrack),
            action = AudMoraPlaybackAction.PlaybackSeekRequested(32f)
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.TrackSelectedForPlayback(nextTrack)
        )

        assertEquals(nextTrack, next.selectedTrack)
        assertEquals(null, next.playbackSeekRequest)
    }

    @Test
    fun playbackCompleted_stopsAndRewindsProgress() {
        val state = testState(
            isPlaying = true,
            playbackSeconds = 24f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackCompleted
        )

        assertFalse(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
    }

    private fun testState(
        selectedTrack: Track = testTrack(id = "track-current"),
        isPlaying: Boolean = false,
        playbackSeconds: Float = 0f
    ): AudMoraUiState {
        return AudMoraUiState(
            tracks = listOf(selectedTrack),
            selectedTrack = selectedTrack,
            isPlaying = isPlaying,
            playbackSeconds = playbackSeconds
        )
    }

    private fun testTrack(id: String): Track {
        return Track(
            id = TrackId(id),
            title = id,
            artist = "Test Artist",
            audioSource = TrackAudioSource.LocalRawResource(resId = id.hashCode())
        )
    }
}
