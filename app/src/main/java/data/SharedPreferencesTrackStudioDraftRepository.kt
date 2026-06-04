package com.opensound.app.data

import android.content.SharedPreferences

class SharedPreferencesTrackStudioDraftRepository(
    sharedPreferences: SharedPreferences
) : TrackStudioDraftRepository by StoredTrackStudioDraftRepository(
    storage = SharedPreferencesAtmosphereConfigStorage(
        sharedPreferences = sharedPreferences,
        key = DraftConfigsKey
    )
) {
    private companion object {
        const val DraftConfigsKey = "track_studio_draft_configs"
    }
}
