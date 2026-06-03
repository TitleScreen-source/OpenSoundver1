package com.opensound.app.data

import android.content.SharedPreferences
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.TrackId

class SharedPreferencesAtmosphereConfigStorage(
    private val sharedPreferences: SharedPreferences,
    private val key: String = SavedAtmosphereConfigsKey
) : AtmosphereConfigStorage {
    override fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig> {
        val savedValue = sharedPreferences.getString(key, null).orEmpty()
        if (savedValue.isBlank()) return emptyMap()

        return savedValue
            .lineSequence()
            .mapNotNull(::decodeEntry)
            .toMap()
    }

    override fun replaceAtmosphereConfigs(configs: Map<TrackId, AtmosphereConfig>) {
        sharedPreferences
            .edit()
            .putString(key, encodeEntries(configs))
            .apply()
    }

    private fun encodeEntries(configs: Map<TrackId, AtmosphereConfig>): String {
        return configs.entries.joinToString(separator = "\n") { (trackId, config) ->
            val encodedTrackId = StorageTextCodec.encode(trackId.value)
            val encodedConfig = StorageTextCodec.encode(
                AtmosphereConfigSerializer.encode(config)
            )

            "$encodedTrackId|$encodedConfig"
        }
    }

    private fun decodeEntry(value: String): Pair<TrackId, AtmosphereConfig>? {
        val separatorIndex = value.indexOf('|')
        if (separatorIndex == -1) return null

        val trackIdValue = StorageTextCodec.decode(value.substring(0, separatorIndex))
            ?: return null
        val configValue = StorageTextCodec.decode(value.substring(separatorIndex + 1))
            ?: return null
        val config = AtmosphereConfigSerializer.decode(configValue)
            ?: return null

        return TrackId(trackIdValue) to config
    }

    private companion object {
        const val SavedAtmosphereConfigsKey = "saved_atmosphere_configs"
    }
}
