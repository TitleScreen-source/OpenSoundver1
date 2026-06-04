package com.opensound.app.data

import android.content.Context

object LocalTrackStudioDraftRepositoryFactory {
    private const val PreferencesName = "audmora_track_studio_drafts"

    fun create(context: Context): TrackStudioDraftRepository {
        val appContext = context.applicationContext

        return SharedPreferencesTrackStudioDraftRepository(
            sharedPreferences = appContext.getSharedPreferences(
                PreferencesName,
                Context.MODE_PRIVATE
            )
        )
    }
}
