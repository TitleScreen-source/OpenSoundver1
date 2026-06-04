package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

class StoredTrackStudioDraftRepository(
    private val storage: AtmosphereConfigStorage
) : TrackStudioDraftRepository {
    override fun draftConfigFor(trackId: TrackId): AtmosphereConfig? {
        return storage.atmosphereConfigs()[trackId]
    }

    override fun saveDraftConfig(
        trackId: TrackId,
        config: AtmosphereConfig
    ) {
        storage.replaceAtmosphereConfigs(
            storage.atmosphereConfigs() + (trackId to config)
        )
    }

    override fun clearDraftConfig(trackId: TrackId) {
        storage.replaceAtmosphereConfigs(
            storage.atmosphereConfigs().filterKeys { savedTrackId ->
                savedTrackId != trackId
            }
        )
    }
}
