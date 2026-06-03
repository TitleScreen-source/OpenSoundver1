package com.opensound.app.data

import com.opensound.app.models.TrackId

class SeedUserLibraryRepository(
    initialSavedTrackIds: List<TrackId> = UserLibrarySeedData.defaultSavedTrackIds
) : UserLibraryRepository by StoredUserLibraryRepository(
    summary = UserLibrarySeedData.summary,
    storage = InMemoryUserLibraryStorage(initialSavedTrackIds)
)
