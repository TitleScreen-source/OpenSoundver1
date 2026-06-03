package com.opensound.app.data

import com.opensound.app.R
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
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

class AudMoraCatalogRepository : TrackRepository {
    override fun tracks(): List<Track> {
        return listOf(
            Track(
                id = AudMoraSeedTrackIds.RezeroShowcase,
                title = "I Feel Sick",
                artist = "Subaru Natsuki",
                audioSource = TrackAudioSource.LocalRawResource(R.raw.rezero_showcase),
                durationSeconds = 35f,
                visualMode = TrackVisualMode.ShowcaseReels
            ),
            Track(
                id = AudMoraSeedTrackIds.NightDrive,
                title = "Night Drive",
                artist = "Synth Waves",
                audioSource = TrackAudioSource.LocalRawResource(R.raw.track1),
                durationSeconds = 209f
            ),
            Track(
                id = AudMoraSeedTrackIds.LostSignal,
                title = "Lost Signal",
                artist = "AUDMORA Artist",
                audioSource = TrackAudioSource.LocalRawResource(R.raw.track1),
                durationSeconds = 209f
            ),
            Track(
                id = AudMoraSeedTrackIds.EchoDreams,
                title = "Echo Dreams",
                artist = "Cyber Pulse",
                audioSource = TrackAudioSource.LocalRawResource(R.raw.track1),
                durationSeconds = 209f
            ),
            Track(
                id = AudMoraSeedTrackIds.MidnightCity,
                title = "Midnight City",
                artist = "Neon Empire",
                audioSource = TrackAudioSource.LocalRawResource(R.raw.track1),
                durationSeconds = 209f
            )
        )
    }

    fun initialAtmosphereConfigs(): Map<TrackId, AtmosphereConfig> {
        return mapOf(
            AudMoraSeedTrackIds.NightDrive to atmospherePresets[0],
            AudMoraSeedTrackIds.LostSignal to atmospherePresets[1],
            AudMoraSeedTrackIds.EchoDreams to atmospherePresets[2],
            AudMoraSeedTrackIds.MidnightCity to atmospherePresets[3]
        )
    }

    override fun audioSourceFor(track: Track): TrackAudioSource {
        return track.audioSource
    }
}
