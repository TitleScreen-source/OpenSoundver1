package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackId

interface TrackRepository {
    fun tracks(): List<Track>

    fun audioResFor(track: Track): Int
}

interface AtmosphereRepository {
    fun initialAtmosphereConfigs(): Map<TrackId, AtmosphereConfig>
}
