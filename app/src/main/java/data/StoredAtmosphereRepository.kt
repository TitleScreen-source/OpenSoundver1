package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

class StoredAtmosphereRepository(
    private val defaultConfigs: Map<TrackId, AtmosphereConfig>,
    private val storage: AtmosphereConfigStorage
) : AtmosphereRepository {
    override fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig> {
        return defaultConfigs + storage.atmosphereConfigs()
    }

    override fun atmosphereConfigFor(trackId: TrackId): AtmosphereConfig? {
        return atmosphereConfigs()[trackId]
    }

    override fun saveAtmosphereConfig(
        trackId: TrackId,
        config: AtmosphereConfig
    ) {
        storage.replaceAtmosphereConfigs(
            storage.atmosphereConfigs() + (trackId to config)
        )
    }
}
