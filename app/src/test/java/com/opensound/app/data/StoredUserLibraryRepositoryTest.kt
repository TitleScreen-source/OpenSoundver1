package com.opensound.app.data

import com.opensound.app.models.TrackId
import com.opensound.app.models.UserLibrarySummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoredUserLibraryRepositoryTest {
    @Test
    fun librarySnapshot_readsSummaryAndStorageTrackIds() {
        val trackId = TrackId("saved-track")
        val repository = repositoryWith(
            initialSavedTrackIds = listOf(trackId)
        )

        val snapshot = repository.librarySnapshot()

        assertEquals("Test library", snapshot.summary.description)
        assertEquals(listOf(trackId), snapshot.savedTrackIds)
    }

    @Test
    fun saveTrack_persistsTrackIdInStorage() {
        val trackId = TrackId("new-track")
        val storage = InMemoryUserLibraryStorage()
        val firstRepository = repositoryWith(storage = storage)

        firstRepository.saveTrack(trackId)
        val secondRepository = repositoryWith(storage = storage)

        assertTrue(secondRepository.isTrackSaved(trackId))
        assertEquals(listOf(trackId), secondRepository.librarySnapshot().savedTrackIds)
    }

    @Test
    fun saveTrack_doesNotDuplicateTrackIds() {
        val trackId = TrackId("new-track")
        val repository = repositoryWith()

        repository.saveTrack(trackId)
        repository.saveTrack(trackId)

        assertEquals(listOf(trackId), repository.librarySnapshot().savedTrackIds)
    }

    @Test
    fun removeSavedTrack_persistsRemovalInStorage() {
        val trackId = TrackId("saved-track")
        val storage = InMemoryUserLibraryStorage(
            initialSavedTrackIds = listOf(trackId)
        )
        val firstRepository = repositoryWith(storage = storage)

        firstRepository.removeSavedTrack(trackId)
        val secondRepository = repositoryWith(storage = storage)

        assertFalse(secondRepository.isTrackSaved(trackId))
        assertTrue(secondRepository.librarySnapshot().savedTrackIds.isEmpty())
    }

    private fun repositoryWith(
        initialSavedTrackIds: List<TrackId> = emptyList(),
        storage: UserLibraryStorage = InMemoryUserLibraryStorage(initialSavedTrackIds)
    ): StoredUserLibraryRepository {
        return StoredUserLibraryRepository(
            summary = UserLibrarySummary("Test library"),
            storage = storage
        )
    }
}
