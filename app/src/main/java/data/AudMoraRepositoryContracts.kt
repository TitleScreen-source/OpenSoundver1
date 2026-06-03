package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.ArtistProfile
import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import com.opensound.app.models.UserLibrarySnapshot
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
    fun librarySnapshot(): UserLibrarySnapshot

    fun saveTrack(trackId: TrackId)

    fun removeSavedTrack(trackId: TrackId)

    fun isTrackSaved(trackId: TrackId): Boolean
}

interface AtmosphereRepository {
    fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig>

    fun atmosphereConfigFor(trackId: TrackId): AtmosphereConfig?

    fun saveAtmosphereConfig(
        trackId: TrackId,
        config: AtmosphereConfig
    )
}
