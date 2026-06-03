package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

class InMemoryAtmosphereRepository(
    initialConfigs: Map<TrackId, AtmosphereConfig> = emptyMap()
) : AtmosphereRepository by StoredAtmosphereRepository(
    defaultConfigs = emptyMap(),
    storage = InMemoryAtmosphereConfigStorage(initialConfigs)
)
