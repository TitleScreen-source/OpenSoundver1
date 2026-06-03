package com.opensound.app.data

import com.opensound.app.models.TrackId
import com.opensound.app.models.UserLibrarySnapshot
import com.opensound.app.models.UserLibrarySummary

class SeedUserLibraryRepository(
    initialSavedTrackIds: List<TrackId> = listOf(
        AudMoraSeedTrackIds.NightDrive,
        AudMoraSeedTrackIds.LostSignal,
        AudMoraSeedTrackIds.EchoDreams
    )
) : UserLibraryRepository {
    private val savedTrackIds = initialSavedTrackIds.distinct().toMutableList()

    override fun librarySnapshot(): UserLibrarySnapshot {
        return UserLibrarySnapshot(
            summary = UserLibrarySummary(
                description = "Треки, плейлисты и атмосферы"
            ),
            savedTrackIds = savedTrackIds.toList()
        )
    }

    override fun saveTrack(trackId: TrackId) {
        if (!isTrackSaved(trackId)) {
            savedTrackIds.add(trackId)
        }
    }

    override fun removeSavedTrack(trackId: TrackId) {
        savedTrackIds.remove(trackId)
    }

    override fun isTrackSaved(trackId: TrackId): Boolean {
        return savedTrackIds.contains(trackId)
    }
}
