package com.opensound.app.data

import com.opensound.app.models.TrackId
import com.opensound.app.models.UserLibrarySnapshot
import com.opensound.app.models.UserLibrarySummary

class StoredUserLibraryRepository(
    private val summary: UserLibrarySummary,
    private val storage: UserLibraryStorage
) : UserLibraryRepository {
    override fun librarySnapshot(): UserLibrarySnapshot {
        return UserLibrarySnapshot(
            summary = summary,
            savedTrackIds = storage.savedTrackIds()
        )
    }

    override fun saveTrack(trackId: TrackId) {
        if (!isTrackSaved(trackId)) {
            storage.replaceSavedTrackIds(storage.savedTrackIds() + trackId)
        }
    }

    override fun removeSavedTrack(trackId: TrackId) {
        storage.replaceSavedTrackIds(
            storage.savedTrackIds().filterNot { savedTrackId ->
                savedTrackId == trackId
            }
        )
    }

    override fun isTrackSaved(trackId: TrackId): Boolean {
        return storage.savedTrackIds().contains(trackId)
    }
}
