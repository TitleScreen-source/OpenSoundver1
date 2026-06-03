package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId

interface TrackRepository {
    fun tracks(): List<Track>

    fun audioSourceFor(track: Track): TrackAudioSource
}

interface TrackFeedRepository {
    fun homeTracks(): List<Track>

    fun artistProfileTracks(): List<Track>

    fun searchTracks(): List<Track>

    fun libraryTracks(): List<Track>

    fun userProfileTracks(): List<Track>
}

interface AtmosphereRepository {
    fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig>

    fun atmosphereConfigFor(trackId: TrackId): AtmosphereConfig?

    fun saveAtmosphereConfig(
        trackId: TrackId,
        config: AtmosphereConfig
    )
}
