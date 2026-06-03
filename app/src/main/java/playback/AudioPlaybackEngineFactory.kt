package com.opensound.app.playback

import android.content.Context
import com.opensound.app.models.TrackAudioSource

fun interface AudioPlaybackEngineFactory {
    fun create(audioSource: TrackAudioSource): AudioPlaybackEngine
}

class AndroidAudioPlaybackEngineFactory(
    private val localRawResourceEngineFactory: (Int) -> AudioPlaybackEngine
) : AudioPlaybackEngineFactory {
    constructor(context: Context) : this(
        localRawResourceEngineFactory = { audioResId ->
            AndroidMediaPlayerAudioEngine.create(
                context = context,
                audioResId = audioResId
            )
        }
    )

    override fun create(audioSource: TrackAudioSource): AudioPlaybackEngine {
        return when (audioSource) {
            is TrackAudioSource.LocalRawResource -> localRawResourceEngineFactory(audioSource.resId)
        }
    }
}
