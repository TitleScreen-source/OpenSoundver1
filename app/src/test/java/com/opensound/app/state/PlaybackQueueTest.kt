package com.opensound.app.state

import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueTest {
    @Test
    fun currentTrack_usesCurrentIndex() {
        val first = testTrack("first")
        val second = testTrack("second")
        val queue = PlaybackQueue(
            tracks = listOf(first, second),
            currentIndex = 1
        )

        assertEquals(second, queue.currentTrack)
    }

    @Test
    fun select_movesToTrackByStableId() {
        val first = testTrack("first")
        val second = testTrack("second")
        val queue = PlaybackQueue(tracks = listOf(first, second))

        val next = queue.select(second)

        assertEquals(second, next.currentTrack)
    }

    @Test
    fun select_ignoresTrackOutsideQueue() {
        val queue = PlaybackQueue(tracks = listOf(testTrack("first")))

        val next = queue.select(testTrack("outside"))

        assertSame(queue, next)
    }

    @Test
    fun skipPreviousAndNext_respectQueueEdges() {
        val first = testTrack("first")
        val second = testTrack("second")
        val queue = PlaybackQueue(tracks = listOf(first, second))

        assertFalse(queue.canSkipPrevious)
        assertTrue(queue.canSkipNext)
        assertEquals(first, queue.skipPrevious().currentTrack)

        val next = queue.skipNext()

        assertTrue(next.canSkipPrevious)
        assertFalse(next.canSkipNext)
        assertEquals(second, next.currentTrack)
        assertEquals(second, next.skipNext().currentTrack)
    }

    @Test
    fun skipPreviousAndNext_wrapWhenRepeatAllIsEnabled() {
        val first = testTrack("first")
        val second = testTrack("second")
        val queue = PlaybackQueue(
            tracks = listOf(first, second),
            repeatMode = PlaybackRepeatMode.All
        )

        assertTrue(queue.canSkipPrevious)
        assertTrue(queue.canSkipNext)
        assertEquals(second, queue.skipPrevious().currentTrack)
        assertEquals(first, queue.skipNext().skipNext().currentTrack)
    }

    @Test
    fun toggleShuffle_keepsCurrentTrackAndUsesShuffledOrder() {
        val first = testTrack("alpha")
        val second = testTrack("bravo")
        val third = testTrack("charlie")
        val queue = PlaybackQueue(tracks = listOf(first, second, third))

        val shuffled = queue.toggleShuffle()

        assertTrue(shuffled.shuffleEnabled)
        assertEquals(first, shuffled.currentTrack)
        assertEquals(third, shuffled.skipNext().currentTrack)
        assertEquals(second, shuffled.skipNext().skipNext().currentTrack)
        assertFalse(shuffled.toggleShuffle().shuffleEnabled)
    }

    @Test
    fun cycleRepeatMode_movesThroughAllModes() {
        val queue = PlaybackQueue(tracks = listOf(testTrack("first")))

        val repeatAll = queue.cycleRepeatMode()
        val repeatOne = repeatAll.cycleRepeatMode()
        val repeatOff = repeatOne.cycleRepeatMode()

        assertEquals(PlaybackRepeatMode.All, repeatAll.repeatMode)
        assertEquals(PlaybackRepeatMode.One, repeatOne.repeatMode)
        assertEquals(PlaybackRepeatMode.Off, repeatOff.repeatMode)
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
