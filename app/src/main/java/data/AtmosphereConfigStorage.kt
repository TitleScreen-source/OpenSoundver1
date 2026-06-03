package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

interface AtmosphereConfigStorage {
    fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig>

    fun replaceAtmosphereConfigs(configs: Map<TrackId, AtmosphereConfig>)
}

class InMemoryAtmosphereConfigStorage(
    initialConfigs: Map<TrackId, AtmosphereConfig> = emptyMap()
) : AtmosphereConfigStorage {
    private val configs = initialConfigs.toMutableMap()

    override fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig> {
        return configs.toMap()
    }

    override fun replaceAtmosphereConfigs(configs: Map<TrackId, AtmosphereConfig>) {
        this.configs.clear()
        this.configs.putAll(configs)
    }
}
