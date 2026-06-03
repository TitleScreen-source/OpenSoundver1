package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryAtmosphereRepositoryTest {
    @Test
    fun atmosphereConfigFor_readsSeededConfigByTrackId() {
        val trackId = TrackId("track-seeded")
        val config = AtmosphereConfig(presetName = "Seeded")
        val repository = InMemoryAtmosphereRepository(
            initialConfigs = mapOf(trackId to config)
        )

        assertEquals(config, repository.atmosphereConfigFor(trackId))
        assertNull(repository.atmosphereConfigFor(TrackId("track-missing")))
    }

    @Test
    fun saveAtmosphereConfig_createsAndReplacesConfigByTrackId() {
        val trackId = TrackId("track-saved")
        val firstConfig = AtmosphereConfig(presetName = "First")
        val secondConfig = AtmosphereConfig(presetName = "Second")
        val repository = InMemoryAtmosphereRepository()

        repository.saveAtmosphereConfig(trackId = trackId, config = firstConfig)
        repository.saveAtmosphereConfig(trackId = trackId, config = secondConfig)

        assertEquals(secondConfig, repository.atmosphereConfigFor(trackId))
        assertEquals(mapOf(trackId to secondConfig), repository.atmosphereConfigs())
    }
}
