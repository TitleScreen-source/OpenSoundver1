package com.opensound.app.playback

import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackControllerTest {
    @Test
    fun synchronize_startsStoppedEngineWhenPlaybackShouldRun() {
        val engine = FakeAudioPlaybackEngine(isPlaying = false)
        val controller = PlaybackController(mediaItem = testMediaItem(), engine = engine)

        controller.synchronize(shouldPlay = true)

        assertTrue(controller.isPlaying)
        assertEquals(1, engine.playCalls)
        assertEquals(0, engine.pauseCalls)
    }

    @Test
    fun synchronize_doesNotRestartAlreadyPlayingEngine() {
        val engine = FakeAudioPlaybackEngine(isPlaying = true)
        val controller = PlaybackController(mediaItem = testMediaItem(), engine = engine)

        controller.synchronize(shouldPlay = true)

        assertTrue(controller.isPlaying)
        assertEquals(0, engine.playCalls)
        assertEquals(0, engine.pauseCalls)
    }

    @Test
    fun synchronize_pausesPlayingEngineWhenPlaybackShouldStop() {
        val engine = FakeAudioPlaybackEngine(isPlaying = true)
        val controller = PlaybackController(mediaItem = testMediaItem(), engine = engine)

        controller.synchronize(shouldPlay = false)

        assertFalse(controller.isPlaying)
        assertEquals(0, engine.playCalls)
        assertEquals(1, engine.pauseCalls)
    }

    @Test
    fun synchronize_doesNotPauseAlreadyStoppedEngine() {
        val engine = FakeAudioPlaybackEngine(isPlaying = false)
        val controller = PlaybackController(mediaItem = testMediaItem(), engine = engine)

        controller.synchronize(shouldPlay = false)

        assertFalse(controller.isPlaying)
        assertEquals(0, engine.playCalls)
        assertEquals(0, engine.pauseCalls)
    }

    @Test
    fun setOnEvent_rewindsEngineAndForwardsCompletedEvent() {
        val engine = FakeAudioPlaybackEngine(isPlaying = true)
        val controller = PlaybackController(mediaItem = testMediaItem(), engine = engine)
        var event: PlaybackEvent? = null

        controller.setOnEvent { playbackEvent ->
            event = playbackEvent
        }
        engine.eventListener?.invoke(PlaybackEvent.Completed)

        assertEquals(1, engine.seekToStartCalls)
        assertEquals(PlaybackEvent.Completed, event)
    }

    @Test
    fun setOnEvent_forwardsErrorWithoutRewinding() {
        val engine = FakeAudioPlaybackEngine(isPlaying = true)
        val controller = PlaybackController(mediaItem = testMediaItem(), engine = engine)
        val error = PlaybackError(code = "test", message = "Test error")
        var event: PlaybackEvent? = null

        controller.setOnEvent { playbackEvent ->
            event = playbackEvent
        }
        engine.eventListener?.invoke(PlaybackEvent.Error(error))

        assertEquals(0, engine.seekToStartCalls)
        assertEquals(PlaybackEvent.Error(error), event)
    }

    @Test
    fun seekTo_returnsEnginePosition() {
        val engine = FakeAudioPlaybackEngine(isPlaying = false)
        val controller = PlaybackController(mediaItem = testMediaItem(), engine = engine)

        val currentPosition = controller.seekTo(12.5f)

        assertEquals(12.5f, currentPosition, 0.001f)
        assertEquals(12.5f, controller.currentPositionSeconds, 0.001f)
        assertEquals(1, engine.seekCalls)
    }

    @Test
    fun release_clearsEventListenerAndReleasesEngine() {
        val engine = FakeAudioPlaybackEngine(isPlaying = false)
        val controller = PlaybackController(mediaItem = testMediaItem(), engine = engine)

        controller.setOnEvent {}
        controller.release()

        assertNull(engine.eventListener)
        assertEquals(1, engine.releaseCalls)
    }

    @Test
    fun defaultFactory_createsControllerForMediaItemThroughEngineFactory() {
        val mediaItem = testMediaItem()
        val expectedEngine = FakeAudioPlaybackEngine(isPlaying = false)
        var requestedMediaItem: PlaybackMediaItem? = null
        val factory = DefaultPlaybackControllerFactory(
            engineFactory = AudioPlaybackEngineFactory { item ->
                requestedMediaItem = item
                expectedEngine
            }
        )

        val controller = factory.create(mediaItem)

        assertSame(mediaItem, requestedMediaItem)
        assertSame(mediaItem, controller.mediaItem)
    }

    private class FakeAudioPlaybackEngine(
        override var isPlaying: Boolean
    ) : AudioPlaybackEngine {
        override var currentPositionSeconds: Float = 0f
        var playCalls = 0
            private set
        var pauseCalls = 0
            private set
        var seekCalls = 0
            private set
        var seekToStartCalls = 0
            private set
        var releaseCalls = 0
            private set
        var eventListener: ((PlaybackEvent) -> Unit)? = null
            private set

        override fun play() {
            playCalls += 1
            isPlaying = true
        }

        override fun pause() {
            pauseCalls += 1
            isPlaying = false
        }

        override fun seekTo(seconds: Float) {
            seekCalls += 1
            if (seconds == 0f) {
                seekToStartCalls += 1
            }
            currentPositionSeconds = seconds.coerceAtLeast(0f)
        }

        override fun release() {
            releaseCalls += 1
        }

        override fun setOnEvent(onEvent: ((PlaybackEvent) -> Unit)?) {
            eventListener = onEvent
        }
    }

    private fun testMediaItem(): PlaybackMediaItem {
        return PlaybackMediaItem(
            id = TrackId("track"),
            title = "Track",
            artist = "Artist",
            durationSeconds = 120f,
            audioSource = TrackAudioSource.LocalRawResource(resId = 42)
        )
    }
}
