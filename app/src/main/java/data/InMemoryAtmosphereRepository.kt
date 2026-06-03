package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

class InMemoryAtmosphereRepository(
    initialConfigs: Map<TrackId, AtmosphereConfig> = emptyMap()
) : AtmosphereRepository {
    private val savedConfigs = initialConfigs.toMutableMap()

    override fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig> {
        return savedConfigs.toMap()
    }

    override fun atmosphereConfigFor(trackId: TrackId): AtmosphereConfig? {
        return savedConfigs[trackId]
    }

    override fun saveAtmosphereConfig(
        trackId: TrackId,
        config: AtmosphereConfig
    ) {
        savedConfigs[trackId] = config
    }
}
