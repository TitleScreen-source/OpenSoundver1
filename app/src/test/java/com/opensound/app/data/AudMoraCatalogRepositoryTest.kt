package com.opensound.app.data

import com.opensound.app.R
import com.opensound.app.models.TrackVisualMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudMoraCatalogRepositoryTest {
    private val repository = AudMoraCatalogRepository()

    @Test
    fun tracks_includeShowcaseReferenceFirst() {
        val tracks = repository.tracks()

        assertEquals(AudMoraSeedTrackIds.RezeroShowcase, tracks.first().id)
        assertEquals(TrackVisualMode.ShowcaseReels, tracks.first().visualMode)
        assertTrue(tracks.first().usesShowcaseVisuals)
        assertEquals("I Feel Sick", tracks.first().title)
    }

    @Test
    fun audioResFor_usesTrackAudioMetadata() {
        val showcaseTrack = repository.tracks().first { track ->
            track.visualMode == TrackVisualMode.ShowcaseReels
        }
        val regularTrack = repository.tracks().first { track ->
            track.visualMode == TrackVisualMode.Atmosphere
        }

        assertEquals(R.raw.rezero_showcase, repository.audioResFor(showcaseTrack))
        assertEquals(R.raw.track1, repository.audioResFor(regularTrack))
    }

    @Test
    fun initialAtmosphereConfigs_useStableTrackIds() {
        val configTrackIds = repository.initialAtmosphereConfigs().keys

        assertTrue(AudMoraSeedTrackIds.NightDrive in configTrackIds)
        assertTrue(AudMoraSeedTrackIds.LostSignal in configTrackIds)
        assertTrue(AudMoraSeedTrackIds.EchoDreams in configTrackIds)
        assertTrue(AudMoraSeedTrackIds.MidnightCity in configTrackIds)
    }

    @Test
    fun catalogRepository_implementsTrackAndAtmosphereContracts() {
        val trackRepository: TrackRepository = repository
        val atmosphereRepository: AtmosphereRepository = repository

        assertTrue(trackRepository.tracks().isNotEmpty())
        assertTrue(atmosphereRepository.initialAtmosphereConfigs().isNotEmpty())
    }
}
