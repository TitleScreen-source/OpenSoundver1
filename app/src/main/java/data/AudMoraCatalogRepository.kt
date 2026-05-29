package com.opensound.app.data

import com.opensound.app.R
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.atmospherePresets

class AudMoraCatalogRepository {
    fun tracks(): List<Track> {
        return listOf(
            Track("I Feel Sick", "Subaru Natsuki", isShowcase = true),
            Track("Night Drive", "Synth Waves"),
            Track("Lost Signal", "AUDMORA Artist"),
            Track("Echo Dreams", "Cyber Pulse"),
            Track("Midnight City", "Neon Empire")
        )
    }

    fun initialAtmosphereConfigs(): Map<String, AtmosphereConfig> {
        return mapOf(
            "Night Drive" to atmospherePresets[0],
            "Lost Signal" to atmospherePresets[1],
            "Echo Dreams" to atmospherePresets[2],
            "Midnight City" to atmospherePresets[3]
        )
    }

    fun audioResFor(track: Track): Int {
        return if (track.isShowcase) {
            R.raw.rezero_showcase
        } else {
            R.raw.track1
        }
    }
}
