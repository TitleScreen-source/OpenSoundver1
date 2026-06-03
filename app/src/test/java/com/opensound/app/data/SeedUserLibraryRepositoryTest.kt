package com.opensound.app.data

import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedUserLibraryRepositoryTest {
    @Test
    fun librarySnapshot_containsDescriptionAndSavedTrackIds() {
        val savedTrackId = TrackId("saved-track")
        val repository = SeedUserLibraryRepository(
            initialSavedTrackIds = listOf(savedTrackId)
        )
        val snapshot = repository.librarySnapshot()

        assertTrue(snapshot.summary.description.isNotBlank())
        assertEquals(listOf(savedTrackId), snapshot.savedTrackIds)
    }

    @Test
    fun saveTrack_addsTrackIdOnce() {
        val trackId = TrackId("new-track")
        val repository = SeedUserLibraryRepository(initialSavedTrackIds = emptyList())

        repository.saveTrack(trackId)
        repository.saveTrack(trackId)

        assertEquals(listOf(trackId), repository.librarySnapshot().savedTrackIds)
        assertTrue(repository.isTrackSaved(trackId))
    }

    @Test
    fun removeSavedTrack_removesTrackId() {
        val trackId = TrackId("saved-track")
        val repository = SeedUserLibraryRepository(
            initialSavedTrackIds = listOf(trackId)
        )

        repository.removeSavedTrack(trackId)

        assertTrue(repository.librarySnapshot().savedTrackIds.isEmpty())
        assertFalse(repository.isTrackSaved(trackId))
    }
}
