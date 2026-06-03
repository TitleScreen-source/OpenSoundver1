package com.opensound.app.data

import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryUserLibraryStorageTest {
    @Test
    fun savedTrackIds_returnsDistinctInitialTrackIds() {
        val trackId = TrackId("track")
        val storage = InMemoryUserLibraryStorage(
            initialSavedTrackIds = listOf(trackId, trackId)
        )

        assertEquals(listOf(trackId), storage.savedTrackIds())
    }

    @Test
    fun replaceSavedTrackIds_replacesPreviousIds() {
        val oldTrackId = TrackId("old")
        val newTrackId = TrackId("new")
        val storage = InMemoryUserLibraryStorage(
            initialSavedTrackIds = listOf(oldTrackId)
        )

        storage.replaceSavedTrackIds(listOf(newTrackId))

        assertEquals(listOf(newTrackId), storage.savedTrackIds())
    }
}
