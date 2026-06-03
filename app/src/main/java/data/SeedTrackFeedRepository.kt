package com.opensound.app.data

import com.opensound.app.models.Track

class SeedTrackFeedRepository(
    private val trackRepository: TrackRepository
) : TrackFeedRepository {
    override fun homeTracks(): List<Track> {
        return trackRepository.tracks()
    }

    override fun artistProfileTracks(): List<Track> {
        return trackRepository.tracks()
    }

    override fun searchTracks(): List<Track> {
        return trackRepository.tracks()
    }

    override fun libraryTracks(): List<Track> {
        return trackRepository.tracks()
    }

    override fun userProfileTracks(): List<Track> {
        return trackRepository.tracks().take(3)
    }
}
