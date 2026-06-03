package com.opensound.app.data

import android.content.Context
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

object LocalAtmosphereRepositoryFactory {
    private const val PreferencesName = "audmora_atmospheres"

    fun create(
        context: Context,
        defaultConfigs: Map<TrackId, AtmosphereConfig>
    ): AtmosphereRepository {
        val appContext = context.applicationContext

        return SharedPreferencesAtmosphereRepository(
            sharedPreferences = appContext.getSharedPreferences(
                PreferencesName,
                Context.MODE_PRIVATE
            ),
            defaultConfigs = defaultConfigs
        )
    }
}
