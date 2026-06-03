package com.opensound.app.data

import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import org.junit.Assert.assertEquals
import org.junit.Test

class SeedTrackFeedRepositoryTest {
    @Test
    fun seedFeeds_useCatalogTracksForMainScreens() {
        val tracks = listOf(
            testTrack("first"),
            testTrack("second"),
            testTrack("third"),
            testTrack("fourth")
        )
        val repository = SeedTrackFeedRepository(FakeTrackRepository(tracks))

        assertEquals(tracks, repository.homeTracks())
        assertEquals(tracks, repository.artistProfileTracks())
        assertEquals(tracks, repository.searchTracks())
        assertEquals(tracks, repository.libraryTracks())
    }

    @Test
    fun userProfileFeed_usesSmallFavoritesSlice() {
        val tracks = listOf(
            testTrack("first"),
            testTrack("second"),
            testTrack("third"),
            testTrack("fourth")
        )
        val repository = SeedTrackFeedRepository(FakeTrackRepository(tracks))

        assertEquals(tracks.take(3), repository.userProfileTracks())
    }

    private class FakeTrackRepository(
        private val tracks: List<Track>
    ) : TrackRepository {
        override fun tracks(): List<Track> {
            return tracks
        }

        override fun audioSourceFor(track: Track): TrackAudioSource {
            return track.audioSource
        }
    }

    private fun testTrack(id: String): Track {
        return Track(
            id = TrackId(id),
            title = id,
            artist = "Test Artist",
            audioSource = TrackAudioSource.LocalRawResource(resId = id.hashCode())
        )
    }
}
