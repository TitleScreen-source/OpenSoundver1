package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.ArtistProfile
import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import com.opensound.app.models.UserLibrarySummary
import com.opensound.app.models.UserProfile

interface TrackRepository {
    fun tracks(): List<Track>

    fun audioSourceFor(track: Track): TrackAudioSource
}

interface TrackFeedRepository {
    fun homeTracks(): List<Track>

    fun artistProfileTracks(): List<Track>

    fun searchTracks(): List<Track>

    fun libraryTracks(): List<Track>

    fun userProfileTracks(): List<Track>
}

interface ProfileRepository {
    fun currentUserProfile(): UserProfile

    fun featuredArtistProfile(): ArtistProfile
}

interface UserLibraryRepository {
    fun librarySummary(): UserLibrarySummary
}

interface AtmosphereRepository {
    fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig>

    fun atmosphereConfigFor(trackId: TrackId): AtmosphereConfig?

    fun saveAtmosphereConfig(
        trackId: TrackId,
        config: AtmosphereConfig
    )
}
