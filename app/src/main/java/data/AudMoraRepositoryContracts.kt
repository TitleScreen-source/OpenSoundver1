package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId

interface TrackRepository {
    fun tracks(): List<Track>

    fun audioSourceFor(track: Track): TrackAudioSource
}

interface AtmosphereRepository {
    fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig>

    fun atmosphereConfigFor(trackId: TrackId): AtmosphereConfig?

    fun saveAtmosphereConfig(
        trackId: TrackId,
        config: AtmosphereConfig
    )
}
