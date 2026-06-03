package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryAtmosphereConfigStorageTest {
    @Test
    fun atmosphereConfigs_readsInitialConfigs() {
        val trackId = TrackId("track")
        val config = AtmosphereConfig(presetName = "Initial")
        val storage = InMemoryAtmosphereConfigStorage(
            initialConfigs = mapOf(trackId to config)
        )

        assertEquals(mapOf(trackId to config), storage.atmosphereConfigs())
    }

    @Test
    fun replaceAtmosphereConfigs_replacesPreviousConfigs() {
        val oldTrackId = TrackId("old")
        val newTrackId = TrackId("new")
        val oldConfig = AtmosphereConfig(presetName = "Old")
        val newConfig = AtmosphereConfig(presetName = "New")
        val storage = InMemoryAtmosphereConfigStorage(
            initialConfigs = mapOf(oldTrackId to oldConfig)
        )

        storage.replaceAtmosphereConfigs(mapOf(newTrackId to newConfig))

        assertEquals(mapOf(newTrackId to newConfig), storage.atmosphereConfigs())
    }
}
