package com.opensound.app.data

import com.opensound.app.R
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackId
import com.opensound.app.models.TrackVisualMode
import com.opensound.app.models.atmospherePresets

object AudMoraSeedTrackIds {
    val RezeroShowcase = TrackId("showcase-rezero-i-feel-sick")
    val NightDrive = TrackId("track-night-drive")
    val LostSignal = TrackId("track-lost-signal")
    val EchoDreams = TrackId("track-echo-dreams")
    val MidnightCity = TrackId("track-midnight-city")
}

class AudMoraCatalogRepository : TrackRepository, AtmosphereRepository {
    override fun tracks(): List<Track> {
        return listOf(
            Track(
                id = AudMoraSeedTrackIds.RezeroShowcase,
                title = "I Feel Sick",
                artist = "Subaru Natsuki",
                audioResId = R.raw.rezero_showcase,
                visualMode = TrackVisualMode.ShowcaseReels
            ),
            Track(
                id = AudMoraSeedTrackIds.NightDrive,
                title = "Night Drive",
                artist = "Synth Waves",
                audioResId = R.raw.track1
            ),
            Track(
                id = AudMoraSeedTrackIds.LostSignal,
                title = "Lost Signal",
                artist = "AUDMORA Artist",
                audioResId = R.raw.track1
            ),
            Track(
                id = AudMoraSeedTrackIds.EchoDreams,
                title = "Echo Dreams",
                artist = "Cyber Pulse",
                audioResId = R.raw.track1
            ),
            Track(
                id = AudMoraSeedTrackIds.MidnightCity,
                title = "Midnight City",
                artist = "Neon Empire",
                audioResId = R.raw.track1
            )
        )
    }

    override fun initialAtmosphereConfigs(): Map<TrackId, AtmosphereConfig> {
        return mapOf(
            AudMoraSeedTrackIds.NightDrive to atmospherePresets[0],
            AudMoraSeedTrackIds.LostSignal to atmospherePresets[1],
            AudMoraSeedTrackIds.EchoDreams to atmospherePresets[2],
            AudMoraSeedTrackIds.MidnightCity to atmospherePresets[3]
        )
    }

    override fun audioResFor(track: Track): Int {
        return track.audioResId
    }
}
