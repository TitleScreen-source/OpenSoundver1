package com.opensound.app.data

import android.content.SharedPreferences
import com.opensound.app.models.TrackId

class SharedPreferencesUserLibraryRepository(
    sharedPreferences: SharedPreferences,
    defaultSavedTrackIds: List<TrackId> = UserLibrarySeedData.defaultSavedTrackIds
) : UserLibraryRepository by StoredUserLibraryRepository(
    summary = UserLibrarySeedData.summary,
    storage = SharedPreferencesUserLibraryStorage(
        sharedPreferences = sharedPreferences,
        defaultSavedTrackIds = defaultSavedTrackIds
    )
)
