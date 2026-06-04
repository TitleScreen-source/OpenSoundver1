package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoredTrackStudioDraftRepositoryTest {
    @Test
    fun draftConfigFor_readsStoredDraftByTrackId() {
        val trackId = TrackId("track")
        val draftConfig = AtmosphereConfig(presetName = "Draft")
        val repository = StoredTrackStudioDraftRepository(
            storage = InMemoryAtmosphereConfigStorage(
                initialConfigs = mapOf(trackId to draftConfig)
            )
        )

        assertEquals(draftConfig, repository.draftConfigFor(trackId))
        assertNull(repository.draftConfigFor(TrackId("missing")))
    }

    @Test
    fun saveDraftConfig_persistsDraftInStorage() {
        val trackId = TrackId("track")
        val draftConfig = AtmosphereConfig(presetName = "Draft")
        val storage = InMemoryAtmosphereConfigStorage()
        val firstRepository = StoredTrackStudioDraftRepository(storage)

        firstRepository.saveDraftConfig(trackId, draftConfig)
        val secondRepository = StoredTrackStudioDraftRepository(storage)

        assertEquals(draftConfig, secondRepository.draftConfigFor(trackId))
    }

    @Test
    fun clearDraftConfig_removesOnlyTargetDraft() {
        val targetTrackId = TrackId("target")
        val otherTrackId = TrackId("other")
        val targetDraft = AtmosphereConfig(presetName = "Target")
        val otherDraft = AtmosphereConfig(presetName = "Other")
        val repository = StoredTrackStudioDraftRepository(
            storage = InMemoryAtmosphereConfigStorage(
                initialConfigs = mapOf(
                    targetTrackId to targetDraft,
                    otherTrackId to otherDraft
                )
            )
        )

        repository.clearDraftConfig(targetTrackId)

        assertNull(repository.draftConfigFor(targetTrackId))
        assertEquals(otherDraft, repository.draftConfigFor(otherTrackId))
    }
}
