package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

class InMemoryTrackStudioDraftRepository(
    initialDrafts: Map<TrackId, AtmosphereConfig> = emptyMap()
) : TrackStudioDraftRepository by StoredTrackStudioDraftRepository(
    storage = InMemoryAtmosphereConfigStorage(initialDrafts)
)
