package com.opensound.app.models

data class ProfileMetric(
    val value: String,
    val label: String
)

data class UserProfile(
    val displayName: String,
    val handle: String,
    val metrics: List<ProfileMetric>
) {
    companion object {
        val Empty = UserProfile(
            displayName = "",
            handle = "",
            metrics = emptyList()
        )
    }
}

data class ArtistProfile(
    val displayName: String,
    val genreLine: String,
    val bio: String
) {
    companion object {
        val Empty = ArtistProfile(
            displayName = "",
            genreLine = "",
            bio = ""
        )
    }
}

data class UserLibrarySummary(
    val description: String
) {
    companion object {
        val Empty = UserLibrarySummary(description = "")
    }
}

data class UserLibrarySnapshot(
    val summary: UserLibrarySummary,
    val savedTrackIds: List<TrackId>
) {
    fun hasSavedTrack(trackId: TrackId): Boolean {
        return savedTrackIds.contains(trackId)
    }

    companion object {
        val Empty = UserLibrarySnapshot(
            summary = UserLibrarySummary.Empty,
            savedTrackIds = emptyList()
        )
    }
}
