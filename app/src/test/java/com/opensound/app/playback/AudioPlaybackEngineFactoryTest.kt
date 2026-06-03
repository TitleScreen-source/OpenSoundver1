package com.opensound.app.playback

import com.opensound.app.models.TrackAudioSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AudioPlaybackEngineFactoryTest {
    @Test
    fun androidFactory_createsLocalRawResourceEngineThroughInjectedFactory() {
        val expectedEngine = FakeAudioPlaybackEngine()
        var requestedAudioResId: Int? = null
        val factory = AndroidAudioPlaybackEngineFactory { audioResId ->
            requestedAudioResId = audioResId
            expectedEngine
        }

        val engine = factory.create(TrackAudioSource.LocalRawResource(resId = 42))

        assertSame(expectedEngine, engine)
        assertEquals(42, requestedAudioResId)
    }

    private class FakeAudioPlaybackEngine : AudioPlaybackEngine {
        override val isPlaying: Boolean = false
        override val currentPositionSeconds: Float = 0f

        override fun play() = Unit

        override fun pause() = Unit

        override fun seekTo(seconds: Float) = Unit

        override fun release() = Unit

        override fun setOnCompletion(onCompleted: (() -> Unit)?) = Unit
    }
}
