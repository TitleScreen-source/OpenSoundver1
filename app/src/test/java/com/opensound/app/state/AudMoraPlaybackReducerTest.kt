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
            tracks = listOf(currentTrack, nextTrack),
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
    fun playbackNextRequested_selectsNextTrackAndRewindsProgress() {
        val currentTrack = testTrack(id = "track-current")
        val nextTrack = testTrack(id = "track-next")
        val state = testState(
            selectedTrack = currentTrack,
            tracks = listOf(currentTrack, nextTrack),
            isPlaying = true,
            playbackSeconds = 42f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackNextRequested
        )

        assertEquals(nextTrack, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
        assertEquals(null, next.playbackSeekRequest)
    }

    @Test
    fun playbackNextRequested_preservesPausedState() {
        val currentTrack = testTrack(id = "track-current")
        val nextTrack = testTrack(id = "track-next")
        val state = testState(
            selectedTrack = currentTrack,
            tracks = listOf(currentTrack, nextTrack),
            isPlaying = false,
            playbackSeconds = 42f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackNextRequested
        )

        assertEquals(nextTrack, next.selectedTrack)
        assertFalse(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
    }

    @Test
    fun playbackPreviousRequested_selectsPreviousTrackAndRewindsProgress() {
        val previousTrack = testTrack(id = "track-previous")
        val currentTrack = testTrack(id = "track-current")
        val stateWithSeek = reduceAudMoraPlaybackState(
            state = testState(
                selectedTrack = currentTrack,
                tracks = listOf(previousTrack, currentTrack),
                isPlaying = true,
                playbackSeconds = 42f
            ),
            action = AudMoraPlaybackAction.PlaybackSeekRequested(18f)
        )

        val next = reduceAudMoraPlaybackState(
            state = stateWithSeek,
            action = AudMoraPlaybackAction.PlaybackPreviousRequested
        )

        assertEquals(previousTrack, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
        assertEquals(null, next.playbackSeekRequest)
    }

    @Test
    fun playbackNextRequested_keepsCurrentTrackAtQueueEnd() {
        val track = testTrack(id = "track-current")
        val state = testState(
            selectedTrack = track,
            tracks = listOf(track),
            isPlaying = true,
            playbackSeconds = 42f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackNextRequested
        )

        assertEquals(track, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(42f, next.playbackSeconds, 0.001f)
    }

    @Test
    fun playbackNextRequested_wrapsAtQueueEndWhenRepeatAllIsEnabled() {
        val firstTrack = testTrack(id = "track-first")
        val secondTrack = testTrack(id = "track-second")
        val state = testState(
            selectedTrack = secondTrack,
            tracks = listOf(firstTrack, secondTrack),
            repeatMode = PlaybackRepeatMode.All,
            isPlaying = true,
            playbackSeconds = 42f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackNextRequested
        )

        assertEquals(firstTrack, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
    }

    @Test
    fun playbackShuffleToggled_updatesQueueMode() {
        val firstTrack = testTrack(id = "track-first")
        val secondTrack = testTrack(id = "track-second")
        val state = testState(
            selectedTrack = firstTrack,
            tracks = listOf(firstTrack, secondTrack)
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackShuffleToggled
        )

        assertTrue(next.shuffleEnabled)
        assertEquals(firstTrack, next.selectedTrack)
    }

    @Test
    fun playbackRepeatModeCycled_updatesQueueMode() {
        val state = testState()

        val repeatAll = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackRepeatModeCycled
        )
        val repeatOne = reduceAudMoraPlaybackState(
            state = repeatAll,
            action = AudMoraPlaybackAction.PlaybackRepeatModeCycled
        )

        assertEquals(PlaybackRepeatMode.All, repeatAll.repeatMode)
        assertEquals(PlaybackRepeatMode.One, repeatOne.repeatMode)
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
    fun playbackProgressChanged_clampsProgressToTrackDuration() {
        val state = testState(
            selectedTrack = testTrack(id = "track-current", durationSeconds = 30f)
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackProgressChanged(42f)
        )

        assertEquals(30f, next.playbackSeconds, 0.001f)
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
    fun playbackSeekRequested_clampsSeekToTrackDuration() {
        val state = testState(
            selectedTrack = testTrack(id = "track-current", durationSeconds = 30f)
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackSeekRequested(42f)
        )

        assertEquals(30f, next.playbackSeconds, 0.001f)
        assertEquals(30f, next.playbackSeekRequest?.seconds ?: -1f, 0.001f)
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
            state = testState(
                selectedTrack = currentTrack,
                tracks = listOf(currentTrack, nextTrack)
            ),
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

    @Test
    fun playbackCompleted_advancesToNextTrackAndKeepsPlaying() {
        val currentTrack = testTrack(id = "track-current")
        val nextTrack = testTrack(id = "track-next")
        val state = testState(
            selectedTrack = currentTrack,
            tracks = listOf(currentTrack, nextTrack),
            isPlaying = true,
            playbackSeconds = 99f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackCompleted
        )

        assertEquals(nextTrack, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
        assertEquals(null, next.playbackSeekRequest)
    }

    @Test
    fun playbackCompleted_wrapsToFirstTrackWhenRepeatAllIsEnabled() {
        val firstTrack = testTrack(id = "track-first")
        val secondTrack = testTrack(id = "track-second")
        val state = testState(
            selectedTrack = secondTrack,
            tracks = listOf(firstTrack, secondTrack),
            repeatMode = PlaybackRepeatMode.All,
            isPlaying = true,
            playbackSeconds = 99f
        )

        val next = reduceAudMoraPlaybackState(
            state = state,
            action = AudMoraPlaybackAction.PlaybackCompleted
        )

        assertEquals(firstTrack, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
    }

    @Test
    fun playbackCompleted_restartsCurrentTrackWhenRepeatOneIsEnabled() {
        val track = testTrack(id = "track-current")
        val stateWithSeekRequest = reduceAudMoraPlaybackState(
            state = testState(
                selectedTrack = track,
                repeatMode = PlaybackRepeatMode.One,
                isPlaying = true,
                playbackSeconds = 99f
            ),
            action = AudMoraPlaybackAction.PlaybackSeekRequested(18f)
        )

        val next = reduceAudMoraPlaybackState(
            state = stateWithSeekRequest,
            action = AudMoraPlaybackAction.PlaybackCompleted
        )

        assertEquals(track, next.selectedTrack)
        assertTrue(next.isPlaying)
        assertEquals(0f, next.playbackSeconds, 0.001f)
        assertEquals(2L, next.playbackSeekRequest?.id)
        assertEquals(0f, next.playbackSeekRequest?.seconds ?: -1f, 0.001f)
    }

    private fun testState(
        selectedTrack: Track = testTrack(id = "track-current"),
        tracks: List<Track> = listOf(selectedTrack),
        repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.Off,
        shuffleEnabled: Boolean = false,
        isPlaying: Boolean = false,
        playbackSeconds: Float = 0f
    ): AudMoraUiState {
        val currentIndex = tracks.indexOfFirst { track -> track.id == selectedTrack.id }
        val queue = PlaybackQueue(
            tracks = tracks,
            currentIndex = currentIndex.coerceAtLeast(0),
            repeatMode = repeatMode
        )

        return AudMoraUiState(
            playbackQueue = if (shuffleEnabled) queue.toggleShuffle() else queue,
            isPlaying = isPlaying,
            playbackSeconds = playbackSeconds
        )
    }

    private fun testTrack(
        id: String,
        durationSeconds: Float = 100f
    ): Track {
        return Track(
            id = TrackId(id),
            title = id,
            artist = "Test Artist",
            audioSource = TrackAudioSource.LocalRawResource(resId = id.hashCode()),
            durationSeconds = durationSeconds
        )
    }
}
