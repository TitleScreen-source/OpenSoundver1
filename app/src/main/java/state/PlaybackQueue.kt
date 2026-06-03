package com.opensound.app.state

import com.opensound.app.models.Track

data class PlaybackQueue(
    val tracks: List<Track>,
    val currentIndex: Int = 0
) {
    init {
        require(tracks.isNotEmpty()) { "PlaybackQueue requires at least one track." }
        require(currentIndex in tracks.indices) { "PlaybackQueue currentIndex must point to an existing track." }
    }

    val currentTrack: Track
        get() = tracks[currentIndex]

    val canSkipPrevious: Boolean
        get() = currentIndex > 0

    val canSkipNext: Boolean
        get() = currentIndex < tracks.lastIndex

    fun select(track: Track): PlaybackQueue {
        val nextIndex = tracks.indexOfFirst { queuedTrack -> queuedTrack.id == track.id }

        return if (nextIndex == -1) {
            this
        } else {
            copy(currentIndex = nextIndex)
        }
    }

    fun skipPrevious(): PlaybackQueue {
        return if (canSkipPrevious) {
            copy(currentIndex = currentIndex - 1)
        } else {
            this
        }
    }

    fun skipNext(): PlaybackQueue {
        return if (canSkipNext) {
            copy(currentIndex = currentIndex + 1)
        } else {
            this
        }
    }
}
