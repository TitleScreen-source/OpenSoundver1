package com.opensound.app.data

import android.content.SharedPreferences
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

class SharedPreferencesAtmosphereRepository(
    sharedPreferences: SharedPreferences,
    defaultConfigs: Map<TrackId, AtmosphereConfig>
) : AtmosphereRepository by StoredAtmosphereRepository(
    defaultConfigs = defaultConfigs,
    storage = SharedPreferencesAtmosphereConfigStorage(sharedPreferences)
)
