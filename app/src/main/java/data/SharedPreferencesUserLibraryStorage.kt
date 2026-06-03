package com.opensound.app.data

import android.content.SharedPreferences
import com.opensound.app.models.TrackId

class SharedPreferencesUserLibraryStorage(
    private val sharedPreferences: SharedPreferences,
    private val key: String = SavedTrackIdsKey,
    private val defaultSavedTrackIds: List<TrackId> = emptyList()
) : UserLibraryStorage {
    override fun savedTrackIds(): List<TrackId> {
        val savedValue = sharedPreferences.getString(key, null)
            ?: return defaultSavedTrackIds.distinct()

        if (savedValue.isBlank()) {
            return emptyList()
        }

        return savedValue
            .lineSequence()
            .filter { value -> value.isNotBlank() }
            .map { value -> TrackId(value) }
            .distinct()
            .toList()
    }

    override fun replaceSavedTrackIds(trackIds: List<TrackId>) {
        sharedPreferences
            .edit()
            .putString(key, serialize(trackIds.distinct()))
            .apply()
    }

    private fun serialize(trackIds: List<TrackId>): String {
        return trackIds.joinToString(separator = "\n") { trackId ->
            trackId.value
        }
    }

    private companion object {
        const val SavedTrackIdsKey = "saved_track_ids"
    }
}
