package com.opensound.app.data

import com.opensound.app.models.TrackId

interface UserLibraryStorage {
    fun savedTrackIds(): List<TrackId>

    fun replaceSavedTrackIds(trackIds: List<TrackId>)
}

class InMemoryUserLibraryStorage(
    initialSavedTrackIds: List<TrackId> = emptyList()
) : UserLibraryStorage {
    private val savedTrackIds = initialSavedTrackIds.distinct().toMutableList()

    override fun savedTrackIds(): List<TrackId> {
        return savedTrackIds.toList()
    }

    override fun replaceSavedTrackIds(trackIds: List<TrackId>) {
        savedTrackIds.clear()
        savedTrackIds.addAll(trackIds.distinct())
    }
}
