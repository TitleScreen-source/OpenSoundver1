package com.opensound.app.state

import com.opensound.app.models.Track

enum class PlaybackQueueSource {
    Catalog,
    Home,
    ArtistProfile,
    Search,
    Library,
    UserProfile
}

enum class PlaybackRepeatMode {
    Off,
    All,
    One;

    fun next(): PlaybackRepeatMode {
        return when (this) {
            Off -> All
            All -> One
            One -> Off
        }
    }
}

data class PlaybackQueue(
    val tracks: List<Track>,
    val currentIndex: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.Off,
    val source: PlaybackQueueSource = PlaybackQueueSource.Catalog,
    private val shuffleOrder: List<Int> = tracks.indices.toList()
) {
    init {
        require(tracks.isNotEmpty()) { "PlaybackQueue requires at least one track." }
        require(currentIndex in tracks.indices) { "PlaybackQueue currentIndex must point to an existing track." }
        require(shuffleOrder.sorted() == tracks.indices.toList()) {
            "PlaybackQueue shuffleOrder must contain every track index exactly once."
        }
    }

    val currentTrack: Track
        get() = tracks[currentIndex]

    val canSkipPrevious: Boolean
        get() = orderPosition > 0 || canWrapQueue

    val canSkipNext: Boolean
        get() = orderPosition < playbackOrder.lastIndex || canWrapQueue

    private val playbackOrder: List<Int>
        get() = if (shuffleEnabled) shuffleOrder else tracks.indices.toList()

    private val orderPosition: Int
        get() = playbackOrder.indexOf(currentIndex)

    private val canWrapQueue: Boolean
        get() = repeatMode == PlaybackRepeatMode.All && tracks.size > 1

    fun select(track: Track): PlaybackQueue {
        val nextIndex = tracks.indexOfFirst { queuedTrack -> queuedTrack.id == track.id }

        return if (nextIndex == -1) {
            this
        } else {
            copy(currentIndex = nextIndex)
        }
    }

    fun replaceContext(
        track: Track,
        queueTracks: List<Track>,
        source: PlaybackQueueSource
    ): PlaybackQueue {
        val normalizedTracks = normalizeQueueTracks(
            queueTracks = queueTracks,
            selectedTrack = track
        )
        val nextIndex = normalizedTracks.indexOfFirst { queuedTrack -> queuedTrack.id == track.id }
        val nextQueue = PlaybackQueue(
            tracks = normalizedTracks,
            currentIndex = nextIndex.coerceAtLeast(0),
            repeatMode = repeatMode,
            source = source
        )

        return if (shuffleEnabled) {
            nextQueue.toggleShuffle()
        } else {
            nextQueue
        }
    }

    fun skipPrevious(): PlaybackQueue {
        return moveBy(offset = -1)
    }

    fun skipNext(): PlaybackQueue {
        return moveBy(offset = 1)
    }

    fun toggleShuffle(): PlaybackQueue {
        return if (shuffleEnabled) {
            copy(
                shuffleEnabled = false,
                shuffleOrder = tracks.indices.toList()
            )
        } else {
            copy(
                shuffleEnabled = true,
                shuffleOrder = buildShuffleOrder()
            )
        }
    }

    fun cycleRepeatMode(): PlaybackQueue {
        return copy(repeatMode = repeatMode.next())
    }

    private fun moveBy(offset: Int): PlaybackQueue {
        val nextOrderPosition = orderPosition + offset
        val nextIndex = when {
            nextOrderPosition in playbackOrder.indices -> playbackOrder[nextOrderPosition]
            canWrapQueue && nextOrderPosition < 0 -> playbackOrder.last()
            canWrapQueue && nextOrderPosition > playbackOrder.lastIndex -> playbackOrder.first()
            else -> currentIndex
        }

        return if (nextIndex == currentIndex) this else copy(currentIndex = nextIndex)
    }

    private fun buildShuffleOrder(): List<Int> {
        val remainingIndices = tracks.indices
            .filterNot { index -> index == currentIndex }
            .sortedByDescending { index -> tracks[index].id.value }

        return listOf(currentIndex) + remainingIndices
    }

    private fun normalizeQueueTracks(
        queueTracks: List<Track>,
        selectedTrack: Track
    ): List<Track> {
        val distinctTracks = queueTracks.distinctBy { track -> track.id }

        return if (distinctTracks.any { track -> track.id == selectedTrack.id }) {
            distinctTracks
        } else {
            listOf(selectedTrack) + distinctTracks
        }
    }
}
