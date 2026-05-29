package com.opensound.app.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioPlaybackEngineTest {
    @Test
    fun synchronizePlayback_startsStoppedEngineWhenPlaybackShouldRun() {
        val engine = FakeAudioPlaybackEngine(isPlaying = false)

        synchronizePlayback(engine = engine, shouldPlay = true)

        assertTrue(engine.isPlaying)
        assertEquals(1, engine.playCalls)
        assertEquals(0, engine.pauseCalls)
    }

    @Test
    fun synchronizePlayback_doesNotRestartAlreadyPlayingEngine() {
        val engine = FakeAudioPlaybackEngine(isPlaying = true)

        synchronizePlayback(engine = engine, shouldPlay = true)

        assertTrue(engine.isPlaying)
        assertEquals(0, engine.playCalls)
        assertEquals(0, engine.pauseCalls)
    }

    @Test
    fun synchronizePlayback_pausesPlayingEngineWhenPlaybackShouldStop() {
        val engine = FakeAudioPlaybackEngine(isPlaying = true)

        synchronizePlayback(engine = engine, shouldPlay = false)

        assertFalse(engine.isPlaying)
        assertEquals(0, engine.playCalls)
        assertEquals(1, engine.pauseCalls)
    }

    @Test
    fun synchronizePlayback_doesNotPauseAlreadyStoppedEngine() {
        val engine = FakeAudioPlaybackEngine(isPlaying = false)

        synchronizePlayback(engine = engine, shouldPlay = false)

        assertFalse(engine.isPlaying)
        assertEquals(0, engine.playCalls)
        assertEquals(0, engine.pauseCalls)
    }

    @Test
    fun handlePlaybackCompletion_rewindsEngineAndNotifiesStateLayer() {
        val engine = FakeAudioPlaybackEngine(isPlaying = true)
        var completed = false

        handlePlaybackCompletion(engine = engine) {
            completed = true
        }

        assertEquals(1, engine.seekToStartCalls)
        assertTrue(completed)
    }

    private class FakeAudioPlaybackEngine(
        override var isPlaying: Boolean
    ) : AudioPlaybackEngine {
        override var currentPositionSeconds: Float = 0f
        var playCalls = 0
            private set
        var pauseCalls = 0
            private set
        var seekToStartCalls = 0
            private set
        var releaseCalls = 0
            private set
        var completionListener: (() -> Unit)? = null
            private set

        override fun play() {
            playCalls += 1
            isPlaying = true
        }

        override fun pause() {
            pauseCalls += 1
            isPlaying = false
        }

        override fun seekToStart() {
            seekToStartCalls += 1
            currentPositionSeconds = 0f
        }

        override fun release() {
            releaseCalls += 1
        }

        override fun setOnCompletion(onCompleted: (() -> Unit)?) {
            completionListener = onCompleted
        }
    }
}
