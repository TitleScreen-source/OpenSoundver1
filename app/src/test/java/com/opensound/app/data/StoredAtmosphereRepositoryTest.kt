package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Test

class StoredAtmosphereRepositoryTest {
    @Test
    fun atmosphereConfigs_mergesDefaultsAndStoredOverrides() {
        val defaultTrackId = TrackId("default-track")
        val savedTrackId = TrackId("saved-track")
        val defaultConfig = AtmosphereConfig(presetName = "Default")
        val savedConfig = AtmosphereConfig(presetName = "Saved")
        val repository = StoredAtmosphereRepository(
            defaultConfigs = mapOf(defaultTrackId to defaultConfig),
            storage = InMemoryAtmosphereConfigStorage(
                initialConfigs = mapOf(savedTrackId to savedConfig)
            )
        )

        assertEquals(
            mapOf(
                defaultTrackId to defaultConfig,
                savedTrackId to savedConfig
            ),
            repository.atmosphereConfigs()
        )
    }

    @Test
    fun storedConfig_overridesDefaultConfigForSameTrack() {
        val trackId = TrackId("track")
        val defaultConfig = AtmosphereConfig(presetName = "Default")
        val savedConfig = AtmosphereConfig(presetName = "Saved")
        val repository = StoredAtmosphereRepository(
            defaultConfigs = mapOf(trackId to defaultConfig),
            storage = InMemoryAtmosphereConfigStorage(
                initialConfigs = mapOf(trackId to savedConfig)
            )
        )

        assertEquals(savedConfig, repository.atmosphereConfigFor(trackId))
    }

    @Test
    fun saveAtmosphereConfig_persistsConfigInStorage() {
        val trackId = TrackId("saved-track")
        val storage = InMemoryAtmosphereConfigStorage()
        val firstRepository = StoredAtmosphereRepository(
            defaultConfigs = emptyMap(),
            storage = storage
        )
        val savedConfig = AtmosphereConfig(presetName = "Saved")

        firstRepository.saveAtmosphereConfig(trackId, savedConfig)
        val secondRepository = StoredAtmosphereRepository(
            defaultConfigs = emptyMap(),
            storage = storage
        )

        assertEquals(savedConfig, secondRepository.atmosphereConfigFor(trackId))
    }
}
