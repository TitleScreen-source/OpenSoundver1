package com.opensound.app.data

import android.content.Context

object LocalUserLibraryRepositoryFactory {
    private const val PreferencesName = "audmora_user_library"

    fun create(context: Context): UserLibraryRepository {
        val appContext = context.applicationContext

        return SharedPreferencesUserLibraryRepository(
            sharedPreferences = appContext.getSharedPreferences(
                PreferencesName,
                Context.MODE_PRIVATE
            )
        )
    }
}
